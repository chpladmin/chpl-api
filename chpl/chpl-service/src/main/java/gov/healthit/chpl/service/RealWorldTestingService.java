package gov.healthit.chpl.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
import gov.healthit.chpl.util.DateUtil;

@Component
public class RealWorldTestingService {
    private static final String CURES_TITLE = "Cures Update";
    public static final String CURES_SUFFIX = " (" + CURES_TITLE + ")";

    @Value("${realWorldTestingCriteriaKeys}")
    private String[] eligibleCriteriaKeys;

    private LocalDate rwtProgramStartDate = LocalDate.of(2018, 9, 1);
    private Integer rwtProgramFirstEligibilityYear = 2019;

    private CertificationCriterionService certificationCriterionService;
    private RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer;
    private ListingActivityUtil listingActivityUtil;
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public RealWorldTestingService(CertificationCriterionService certificationCriterionService,
            RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer, ListingActivityUtil listingActivityUtil,
            CertifiedProductDAO certifiedProductDAO) {
        this.certificationCriterionService = certificationCriterionService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public Optional<Integer> getRwtEligibilityYearForListing(Long listingId, Logger logger) {
        return getRwtEligibilityYearForListing(listingId, true, logger);
    }

    public Optional<Integer> getRwtEligibilityYearForListing(Long listingId, boolean log, Logger logger) {
        LocalDate currentRwtEligStartDate = rwtProgramStartDate;
        Integer currentRwtEligYear = rwtProgramFirstEligibilityYear;

        while (currentRwtEligStartDate.isBefore(LocalDate.now())) {
            Optional<CertifiedProductSearchDetails> listing = getListingAsOfDate(listingId, currentRwtEligStartDate);

            if (listing.isPresent()) {
                Optional<Integer> rwtEligYearBasedOnIcs = getRwtEligibilityYearBasedOnIcs(listing.get(), logger);
                if (rwtEligYearBasedOnIcs.isPresent()) {
                    if (log) {
                        logger.info(String.format("ListingId: %s - Eligibility Year calculated using ICS - %s", listing.get().getId(), rwtEligYearBasedOnIcs.get()));
                    }
                    return rwtEligYearBasedOnIcs;
                } else if (isListingRwtEligible(listing.get(), currentRwtEligStartDate)) {
                    if (log) {
                        logger.info(String.format("ListingId: %s - Eligibility Year - %s", listing.get().getId(), currentRwtEligYear));
                    }
                    return Optional.of(currentRwtEligYear);
                }
            }
            //Check the next year...
            currentRwtEligStartDate = currentRwtEligStartDate.plusYears(1L);
            currentRwtEligYear++;
        }
        if (log) {
            logger.info(String.format("ListingId: %s - Not Eligible", listingId));
        }
        return Optional.empty();
    }

    private Optional<Integer> getRwtEligibilityYearBasedOnIcs(CertifiedProductSearchDetails listing, Logger logger) {
        try {
            if (listing.getIcs().getParents() != null
                    && listing.getIcs().getParents().size() > 0
                    && doesListingAttestToEligibleCriteria(listing)) {

                //Need a "details" object for the icsCode
                CertifiedProductDTO cpChild = certifiedProductDAO.getById(listing.getId());
                List<Integer> parentEligibilityYears = new ArrayList<Integer>();

                for (CertifiedProduct cp : listing.getIcs().getParents()) {
                    //Need a "details" object for the icsCode
                    CertifiedProductDTO cpParent = certifiedProductDAO.getById(cp.getId());

                    //This helps break any ics "loops" that may exist
                    if (Integer.valueOf(cpParent.getIcsCode()) >= Integer.valueOf(cpChild.getIcsCode())) {
                        continue;
                    }
                    //Get the eligiblity year for the parent...  Uh-oh - possible recursion...
                    Optional<Integer> parentEligibilityYear = getRwtEligibilityYearForListing(cp.getId(), false, logger);
                    if (parentEligibilityYear.isPresent()) {
                        parentEligibilityYears.add(parentEligibilityYear.get());
                    }
                }
                if (parentEligibilityYears.size() > 0) {
                    return parentEligibilityYears.stream()
                            .min(Integer::compare);
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

    private boolean isListingRwtEligible(CertifiedProductSearchDetails listing, LocalDate asOfDate) {
        return isListingStatusActiveAsOfEligibilityDate(listing, asOfDate)
                && isCertificationDateBeforeEligibilityDate(listing, asOfDate)
                && doesListingAttestToEligibleCriteria(listing);

    }

    private boolean doesListingAttestToEligibleCriteria(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> eligibleCriteria = getRwtEligibleCriteria();
        boolean doesExist = listing.getCertificationResults().stream()
                .filter(result -> result.isSuccess()
                        && eligibleCriteria.stream()
                        .filter(crit -> crit.getId().equals(result.getCriterion().getId()))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();

        if (doesExist) {
            return true;
        } else {
            //LOGGER.info("Listing: " + listing.getId() + " - Does not attest to any eligible criteria");
            return false;
        }
    }


    private List<CertificationCriterion> getRwtEligibleCriteria() {
        return Arrays.asList(eligibleCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }

    private boolean isCertificationDateBeforeEligibilityDate(CertifiedProductSearchDetails listing, LocalDate eligibilityDate) {
        if (Objects.isNull(listing) || Objects.isNull(listing.getCertificationDate())) {
            //LOGGER.info("Listing: " + listing.getId() + " - Certification date does not exist");
            return false;
        } else {
            LocalDate certDate = DateUtil.toLocalDate(listing.getCertificationDate());
            if (certDate.isBefore(eligibilityDate)) {
                return true;
            } else {
                //LOGGER.info("Listing: " + listing.getId() + " - Certification date is after eligibility start date");
                return false;
            }
        }
    }

    private boolean isListingStatusActiveAsOfEligibilityDate(CertifiedProductSearchDetails listing, LocalDate eligibilityDate) {
        CertificationStatusEvent event = listing.getStatusOnDate(convertLocalDateToDateUtcAtMidnight(eligibilityDate));
        if (Objects.nonNull(event)
                && event.getStatus().getName().equals(CertificationStatusType.Active.getName())) {
            return true;
        } else {
            //LOGGER.info("Listing: " + listing.getId() + " - Not Active");
            return false;
        }
    }

    private Date convertLocalDateToDateUtcAtMidnight(LocalDate localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth(), 0, 0, 0);
        return calendar.getTime();
    }
}
