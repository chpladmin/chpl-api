package gov.healthit.chpl.service.realworldtesting;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.RealWorldTestingEligibilityActivityExplorer;
import gov.healthit.chpl.activity.history.query.RealWorldTestingEligibilityQuery;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.RealWorldTestingEligibility;
import gov.healthit.chpl.service.RealWorldTestingEligiblityReason;
import gov.healthit.chpl.util.DateUtil;

// This class *should* only be instantiated by RealWorldTestingServiceFactory, so that the memoization is threadsafe.
// To get an instance of this class use RealWorldTestingServiceFactory.getInstance().
public class RealWorldTestingEligiblityService {
    private String[] eligibleCriteriaKeys;
    private LocalDate rwtProgramStartDate;
    private Integer rwtProgramFirstEligibilityYear;
    private CertificationCriterionService certificationCriterionService;
    private RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer;
    private ListingActivityUtil listingActivityUtil;
    private CertifiedProductDAO certifiedProductDAO;

    private Map<Long, RealWorldTestingEligibility> memo = new HashMap<Long, RealWorldTestingEligibility>();

    public RealWorldTestingEligiblityService(CertificationCriterionService certificationCriterionService,
            RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer, ListingActivityUtil listingActivityUtil,
            CertifiedProductDAO certifiedProductDAO, String[] eligibleCriteriaKeys, LocalDate rwtProgramStartDate, Integer rwtProgramFirstEligibilityYear) {
        this.certificationCriterionService = certificationCriterionService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
        this.certifiedProductDAO = certifiedProductDAO;
        this.eligibleCriteriaKeys = eligibleCriteriaKeys;
        this.rwtProgramStartDate = rwtProgramStartDate;
        this.rwtProgramFirstEligibilityYear = rwtProgramFirstEligibilityYear;
    }

    public RealWorldTestingEligibility getRwtEligibilityYearForListing(Long listingId, Logger logger) {
        //Check to see if we have already calculated the eligibility for this listing.  Because of the ICS
        //relationships, we need to use recursion and it can be very slow.
        if (memo.containsKey(listingId)) {
            return memo.get(listingId);
        }

        Optional<Integer> rwtEligYearBasedOnIcs = getRwtEligibilityYearBasedOnIcs(listingId, logger);
        if (rwtEligYearBasedOnIcs.isPresent()) {
            RealWorldTestingEligibility eligibility = new RealWorldTestingEligibility(RealWorldTestingEligiblityReason.ICS, rwtEligYearBasedOnIcs);
            addCalculatedResultsToMemo(listingId, eligibility);
            return eligibility;
        } else {
            Optional<RealWorldTestingEligibility> rwtElig = getRwtEligBasedOnStandardRequirements(listingId);
            if (rwtElig.isPresent()) {
                return rwtElig.get();

            }
        }
        RealWorldTestingEligibility eligibility = new RealWorldTestingEligibility(RealWorldTestingEligiblityReason.NOT_ELIGIBLE, Optional.empty());
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
        LocalDate currentRwtEligStartDate = rwtProgramStartDate;
        Integer currentRwtEligYear = rwtProgramFirstEligibilityYear;
        while (currentRwtEligStartDate.isBefore(LocalDate.now())) {
            Optional<CertifiedProductSearchDetails> listing = getListingAsOfDate(listingId, currentRwtEligStartDate);
            if (listing.isPresent() && isListingRwtEligible(listing.get(), currentRwtEligStartDate)) {
                RealWorldTestingEligibility eligibility = new RealWorldTestingEligibility(RealWorldTestingEligiblityReason.SELF, Optional.of(currentRwtEligYear));
                addCalculatedResultsToMemo(listingId, eligibility);
                return Optional.of(eligibility);
            }
            //Eligibility could not be determined, check the next year...
            currentRwtEligStartDate = currentRwtEligStartDate.plusYears(1L);
            currentRwtEligYear++;
        }
        return Optional.empty();
    }

    private Optional<Integer> getRwtEligibilityYearBasedOnIcs(Long listingId, Logger logger) {
        try {
            Optional<CertifiedProductSearchDetails> listing = getListingInOriginalState(listingId);
            if (listing.isPresent()) {
                // If the listing is certified before the program start date it is not eligible for RWT elig based on ICS.
                if (isDateAfterOrEqualToRwtProgramStartDate(listing.get().getCertificationDate())
                    && listing.get().getIcs().getParents() != null
                    && listing.get().getIcs().getParents().size() > 0
                    && doesListingAttestToEligibleCriteria(listing.get())) {

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
                        //Get the eligiblity year for the parent...  Uh-oh - possible recursion...
                        RealWorldTestingEligibility parentEligibility = getRwtEligibilityYearForListing(cpParent.getId(), logger);
                        if (parentEligibility.getEligibilityYear().isPresent()) {
                            parentEligibilityYears.add(parentEligibility.getEligibilityYear().get());
                        }
                    }
                    if (parentEligibilityYears.size() > 0) {
                        return parentEligibilityYears.stream()
                                .min(Integer::compare);
                    }
                }
            }
            return Optional.empty();
        } catch (EntityRetrievalException e) {
            return Optional.empty();
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
                && doesListingAttestToEligibleCriteria(listing);

    }

    private boolean doesListingAttestToEligibleCriteria(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> eligibleCriteria = getRwtEligibleCriteria();
        return listing.getCertificationResults().stream()
                .filter(result -> result.isSuccess()
                        && eligibleCriteria.stream()
                        .filter(crit -> crit.getId().equals(result.getCriterion().getId()))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();
    }


    private List<CertificationCriterion> getRwtEligibleCriteria() {
        return Arrays.asList(eligibleCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
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
        return Objects.nonNull(event) && event.getStatus().getName().equals(CertificationStatusType.Active.getName());
    }

    private Date convertLocalDateToDateUtcAtMidnight(LocalDate localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth(), 0, 0, 0);
        return calendar.getTime();
    }
}
