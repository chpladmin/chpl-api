package gov.healthit.chpl.service.realworldtesting;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.RealWorldTestingEligibilityActivityExplorer;
import gov.healthit.chpl.activity.history.query.RealWorldTestingEligibilityQuery;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This class *should* only be instantiated by RealWorldTestingServiceFactory, so that the memoization is threadsafe.
// To get an instance of this class use RealWorldTestingServiceFactory.getInstance().
public class RealWorldTestingEligiblityService {
    private RealWorldTestingCriteriaService realWorldTestingCriteriaService;
    private LocalDate rwtProgramStartDate;
    private Integer rwtProgramFirstEligibilityYear;
    private RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer;
    private ListingActivityUtil listingActivityUtil;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationCriterionService criteriaService;
    private FF4j ff4j;

    private Map<Long, RealWorldTestingEligibility> memo = new HashMap<Long, RealWorldTestingEligibility>();

    public RealWorldTestingEligiblityService(RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer,
            ListingActivityUtil listingActivityUtil,
            CertifiedProductDAO certifiedProductDAO,
            LocalDate rwtProgramStartDate,
            Integer rwtProgramFirstEligibilityYear,
            CertificationCriterionService criteriaService,
            FF4j ff4j) {
        this.realWorldTestingCriteriaService = realWorldTestingCriteriaService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
        this.certifiedProductDAO = certifiedProductDAO;
        this.rwtProgramStartDate = rwtProgramStartDate;
        this.rwtProgramFirstEligibilityYear = rwtProgramFirstEligibilityYear;
        this.criteriaService = criteriaService;
        this.ff4j = ff4j;
    }

    public RealWorldTestingEligibility getRwtEligibilityYearForListing(Long listingId, Logger logger) {
        //Check to see if we have already calculated the eligibility for this listing.  Because of the ICS
        //relationships, we need to use recursion and it can be very slow.
        if (memo.containsKey(listingId)) {
            return memo.get(listingId);
        }

        RealWorldTestingEligibilityIcs rwtEligBasedOnIcs = getRwtEligibilityBasedOnIcs(listingId, logger);
        if (rwtEligBasedOnIcs != null) {
            RealWorldTestingEligibility eligibility
                = getRwtEligibility(rwtEligBasedOnIcs.getListing(), RealWorldTestingEligiblityReason.ICS, rwtEligBasedOnIcs.getEligibilityYear());
            addCalculatedResultsToMemo(listingId, eligibility);
            return eligibility;
        } else {
            Optional<RealWorldTestingEligibility> rwtElig = getRwtEligBasedOnStandardRequirements(listingId);
            if (rwtElig.isPresent()) {
                return rwtElig.get();
            }
        }
        RealWorldTestingEligibility eligibility = getRwtEligibility(null, RealWorldTestingEligiblityReason.NOT_ELIGIBLE, null);
        addCalculatedResultsToMemo(listingId, eligibility);
        return eligibility;
    }

    private boolean isDateAfterOrEqualToRwtProgramStartDate(Long dateToCheck) {
        return isDateAfterOrEqualToRwtProgramStartDate(DateUtil.toLocalDate(dateToCheck));
    }

    private boolean isDateAfterOrEqualToRwtProgramStartDate(LocalDate dateToCheck) {
        return dateToCheck.isAfter(rwtProgramStartDate) || dateToCheck.equals(rwtProgramStartDate);
    }

    private void addCalculatedResultsToMemo(Long listingId, RealWorldTestingEligibility eligibility) {
        memo.put(listingId, eligibility);
    }

    private Optional<RealWorldTestingEligibility> getRwtEligBasedOnStandardRequirements(Long listingId) {
        //Initially try to determine the eligibility based on the beginning of the program
        LocalDate currentRwtEligStartDate = rwtProgramStartDate; //9/1/2021
        Integer currentRwtEligYear = rwtProgramFirstEligibilityYear; //2022
        while (currentRwtEligStartDate.isBefore(LocalDate.now())) {
            Optional<CertifiedProductSearchDetails> listing = getListingAsOfDateOrOriginalState(listingId, currentRwtEligStartDate);
            if (listing.isPresent() && isListingRwtEligible(listing.get(), currentRwtEligStartDate)) {
                RealWorldTestingEligibility eligibility = getRwtEligibility(listing, RealWorldTestingEligiblityReason.SELF, currentRwtEligYear);
                addCalculatedResultsToMemo(listingId, eligibility);
                return Optional.of(eligibility);
            }
            //Eligibility could not be determined, check the next year...
            currentRwtEligStartDate = currentRwtEligStartDate.plusYears(1L);
            currentRwtEligYear++;
        }
        return Optional.empty();
    }

    private RealWorldTestingEligibility getRwtEligibility(Optional<CertifiedProductSearchDetails> listing,
            RealWorldTestingEligiblityReason reason,
            Integer currentRwtEligYear) {
        List<CertificationResult> attestedCertificationResults = new ArrayList<CertificationResult>();
        if (listing != null && listing.isPresent()) {
            attestedCertificationResults = listing.get().getCertificationResults().stream()
                    //We might be getting this listing in it's original state from saved JSON.
                    //For most of CHPL before mid-2023, we saved a certification result on the listing for each criteria
                    //and used the "success" field to determine if that listing attested to that criterion.
                    //If we have pulled listing JSON pre-that-time, then we must check the success field here.
                    //We still have "success" today, but it is always "true" because after mid-2023, we changed our
                    //listing details response to only include certification results for each criterion the listing attests to.
                    .filter(certResult -> BooleanUtils.isTrue(certResult.getSuccess()))
                    .collect(Collectors.toList());
        }
        return RealWorldTestingEligibility.builder()
            .reason(reason)
            .eligibilityYear(currentRwtEligYear)
            .attestedCertificationResults(attestedCertificationResults)
        .build();
    }

    private Optional<CertifiedProductSearchDetails> getListingAsOfDateOrOriginalState(Long listingId, LocalDate asOfDate) {
        Optional<CertifiedProductSearchDetails> listing = getListingAsOfDate(listingId, asOfDate);
        if (listing.isEmpty()) {
            listing = getListingInOriginalState(listingId);
        }
        return listing;
    }

    private RealWorldTestingEligibilityIcs getRwtEligibilityBasedOnIcs(Long listingId, Logger logger) {
        try {
            Optional<CertifiedProductSearchDetails> listing = getListingInOriginalState(listingId);
            if (listing.isPresent()) {
                // If the listing is certified before the program start date it is not eligible for RWT elig based on ICS.
                if (isDateAfterOrEqualToRwtProgramStartDate(listing.get().getCertificationDate())
                    && listing.get().getIcs().getParents() != null
                    && listing.get().getIcs().getParents().size() > 0) {

                    //Need a "details" object for the icsCode
                    CertifiedProductDTO cpChild = certifiedProductDAO.getById(listing.get().getId());
                    List<Integer> parentEligibilityYears = new ArrayList<Integer>();
                    for (CertifiedProduct cpParent : listing.get().getIcs().getParents()) {
                        //Need a "details" object for the icsCode
                        CertifiedProductDTO cpParentDto = certifiedProductDAO.getById(cpParent.getId());
                        //This helps break any ics "loops" that may exist
                        if (Integer.valueOf(cpParentDto.getIcsCode()) >= Integer.valueOf(cpChild.getIcsCode())) {
                            continue;
                        }
                        //Uh-oh - possible recursion...
                        RealWorldTestingEligibility parentEligibility = getRwtEligibilityYearForListing(cpParent.getId(), logger);
                        if (parentEligibility.getEligibilityYear() != null
                                && doesListingAttestToEligibleCriteria(listing.get(), parentEligibility.getEligibilityYear())) {
                            parentEligibilityYears.add(parentEligibility.getEligibilityYear());
                        }
                    }
                    if (parentEligibilityYears.size() > 0) {
                        Optional<Integer> minEligibilityYear = parentEligibilityYears.stream()
                                .min(Integer::compare);

                        if (minEligibilityYear.isPresent()) {
                            return RealWorldTestingEligibilityIcs.builder()
                                    .eligibilityYear(minEligibilityYear.get())
                                    .listing(listing)
                                    .build();
                        }
                        return null;
                    }
                }
            }
            return null;
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    private Optional<CertifiedProductSearchDetails> getListingAsOfDate(Long listingId, LocalDate asOfDate) {
        RealWorldTestingEligibilityQuery query = new RealWorldTestingEligibilityQuery(listingId, asOfDate);
        ActivityDTO activity = realWorldTestingEligibilityActivityExplorer.getActivity(query);
        if (activity == null) {
            return Optional.empty();
        } else {
            CertifiedProductSearchDetails listing = listingActivityUtil.getListing(activity.getNewData(), true);
            return Optional.of(listing);
        }
    }

    private Optional<CertifiedProductSearchDetails> getListingInOriginalState(Long listingId) {
        return getListingAsOfDate(listingId, null);
    }


    private boolean isListingRwtEligible(CertifiedProductSearchDetails listing, LocalDate asOfDate) {
        return isListingStatusActiveAsOfEligibilityDate(listing, asOfDate)
                && isCertificationDateBeforeEligibilityDate(listing, asOfDate)
                && doesListingAttestToEligibleCriteria(listing, asOfDate.getYear());
    }

    private boolean doesListingAttestToEligibleCriteria(CertifiedProductSearchDetails listing, Integer year) {
        List<CertificationCriterion> eligibleCriteria = realWorldTestingCriteriaService.getEligibleCriteria(year);
        return listing.getCertificationResults().stream()
                .filter(result -> result.getSuccess()
                        && eligibleCriteria.stream()
                        .filter(crit -> crit.getId().equals(result.getCriterion().getId()) && isGCriteriaOrUsesSvap(result))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();
    }

    private boolean isGCriteriaOrUsesSvap(CertificationResult certResult) {
        if (ff4j.check(FeatureList.HTI_5_ERD)) {
            return criteriaService.isGCriterion(certResult.getCriterion()) || !CollectionUtils.isEmpty(certResult.getSvaps());
        }
        return true;
    }

    private boolean isCertificationDateBeforeEligibilityDate(CertifiedProductSearchDetails listing, LocalDate eligibilityDate) {
        if (Objects.isNull(listing) || Objects.isNull(listing.getCertificationDate())) {
            return false;
        } else {
            LocalDate certDate = DateUtil.toLocalDate(listing.getCertificationDate());
            return certDate.isBefore(eligibilityDate);
        }
    }

    private boolean isListingStatusActiveAsOfEligibilityDate(CertifiedProductSearchDetails listing, LocalDate eligibilityDate) {
        CertificationStatusEvent event = listing.getStatusOnDate(convertLocalDateToDateUtcAtMidnight(eligibilityDate));
        return Objects.nonNull(event)
                && isActive(event.getStatus().getName());
    }

    private boolean isActive(String statusName) {
        return CertificationStatusUtil.getActiveStatusNames().contains(statusName);
    }

    private Date convertLocalDateToDateUtcAtMidnight(LocalDate localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth(), 0, 0, 0);
        return calendar.getTime();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static final class RealWorldTestingEligibilityIcs {
        private Integer eligibilityYear;
        private Optional<CertifiedProductSearchDetails> listing;
    }
}
