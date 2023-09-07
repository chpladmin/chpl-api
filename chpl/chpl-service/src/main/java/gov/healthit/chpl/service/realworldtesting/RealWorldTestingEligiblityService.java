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

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.RealWorldTestingEligibilityActivityExplorer;
import gov.healthit.chpl.activity.history.query.RealWorldTestingEligibilityQuery;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;

// This class *should* only be instantiated by RealWorldTestingServiceFactory, so that the memoization is threadsafe.
// To get an instance of this class use RealWorldTestingServiceFactory.getInstance().
public class RealWorldTestingEligiblityService {
    private RealWorldTestingCriteriaService realWorldTestingCriteriaService;
    private LocalDate rwtProgramStartDate;
    private Integer rwtProgramFirstEligibilityYear;
    private RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer;
    private CertificationStatusEventsService certStatusService;
    private ListingActivityUtil listingActivityUtil;
    private CertifiedProductDAO certifiedProductDAO;

    private Map<Long, RealWorldTestingEligibility> memo = new HashMap<Long, RealWorldTestingEligibility>();
    private List<CertificationStatusType> withdrawnStatuses;

    public RealWorldTestingEligiblityService(RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer,
            CertificationStatusEventsService certStatusService, ListingActivityUtil listingActivityUtil,
            CertifiedProductDAO certifiedProductDAO, LocalDate rwtProgramStartDate, Integer rwtProgramFirstEligibilityYear) {
        this.realWorldTestingCriteriaService = realWorldTestingCriteriaService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certStatusService = certStatusService;
        this.rwtProgramStartDate = rwtProgramStartDate;
        this.rwtProgramFirstEligibilityYear = rwtProgramFirstEligibilityYear;

        withdrawnStatuses = List.of(CertificationStatusType.WithdrawnByDeveloper,
                CertificationStatusType.WithdrawnByAcb,
                CertificationStatusType.WithdrawnByDeveloperUnderReview,
                CertificationStatusType.Retired,
                CertificationStatusType.TerminatedByOnc);
    }

    public RealWorldTestingEligibility getRwtEligibilityYearForListing(Long listingId, Logger logger) {
        //Check to see if we have already calculated the eligibility for this listing.  Because of the ICS
        //relationships, we need to use recursion and it can be very slow.
        if (memo.containsKey(listingId)) {
            return memo.get(listingId);
        }

        Integer rwtEligYearBasedOnIcs = getRwtEligibilityYearBasedOnIcs(listingId, logger);
        if (rwtEligYearBasedOnIcs != null) {
            RealWorldTestingEligibility eligibility = new RealWorldTestingEligibility(RealWorldTestingEligiblityReason.ICS, rwtEligYearBasedOnIcs);
            addCalculatedResultsToMemo(listingId, eligibility);
            return eligibility;
        } else {
            Optional<RealWorldTestingEligibility> rwtElig = getRwtEligBasedOnStandardRequirements(listingId);
            if (rwtElig.isPresent()) {
                return rwtElig.get();
            }
        }
        RealWorldTestingEligibility eligibility = new RealWorldTestingEligibility(RealWorldTestingEligiblityReason.NOT_ELIGIBLE, null);
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
                RealWorldTestingEligibility eligibility = new RealWorldTestingEligibility(RealWorldTestingEligiblityReason.SELF, currentRwtEligYear);
                addCalculatedResultsToMemo(listingId, eligibility);
                return Optional.of(eligibility);
            }
            //Eligibility could not be determined, check the next year...
            currentRwtEligStartDate = currentRwtEligStartDate.plusYears(1L);
            currentRwtEligYear++;
        }
        return Optional.empty();
    }

    private Integer getRwtEligibilityYearBasedOnIcs(Long listingId, Logger logger) {
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
                        if (listingIsWithdrawn(cpParentDto, logger)) {
                            //If parent is withdrawn continue with calculating its eligibility year.... Uh-oh - possible recursion...
                            RealWorldTestingEligibility parentEligibility = getRwtEligibilityYearForListing(cpParent.getId(), logger);
                            if (parentEligibility.getEligibilityYear() != null
                                    && doesListingAttestToEligibleCriteria(listing.get(), parentEligibility.getEligibilityYear())) {
                                parentEligibilityYears.add(parentEligibility.getEligibilityYear());
                            }
                        }
                    }
                    if (parentEligibilityYears.size() > 0) {
                        Optional<Integer> minEligibilityYear = parentEligibilityYears.stream()
                                .min(Integer::compare);
                        return minEligibilityYear.isPresent() ? minEligibilityYear.get() : null;
                    }
                }
            }
            return null;
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    private boolean listingIsWithdrawn(CertifiedProductDTO listing, Logger logger) {
        boolean result = false;
        try {
            CertificationStatusEvent currentStatusEvent = certStatusService.getCurrentCertificationStatusEvent(listing.getId());
            logger.debug("Listing " + listing.getId() + " has current certification status of " + currentStatusEvent.getStatus().getName());
            result = currentStatusEvent != null && withdrawnStatuses.stream()
                    .map(status -> status.getName())
                    .filter(statusName -> currentStatusEvent.getStatus().getName().equals(statusName))
                    .findAny().isPresent();
        } catch (EntityRetrievalException ex) {
            logger.error("Unable to get current certification status event for listing  " + listing.getId(), ex);
            return result;
        }
        return result;
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
                .filter(result -> result.isSuccess()
                        && eligibleCriteria.stream()
                        .filter(crit -> crit.getId().equals(result.getCriterion().getId()))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();
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
}
