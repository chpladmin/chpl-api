package gov.healthit.chpl.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.RealWorldTestingEligibilityActivityExplorer;
import gov.healthit.chpl.activity.history.query.RealWorldTestingEligibilityQuery;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
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

    @Autowired
    public RealWorldTestingService(CertificationCriterionService certificationCriterionService,
            RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer, ListingActivityUtil listingActivityUtil) {
        this.certificationCriterionService = certificationCriterionService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
    }

    public Optional<Integer> getRwtEligibilityYearForListing(Long listingId) {
        LocalDate currentRwtEligStartDate = rwtProgramStartDate;
        Integer currentRwtEligYear = rwtProgramFirstEligibilityYear;

        while (currentRwtEligStartDate.isBefore(LocalDate.now())) {
            Optional<CertifiedProductSearchDetails> listing = getListingAsOfDate(listingId, currentRwtEligStartDate);

            if (listing.isPresent() && isListingRwtEligible(listing.get(), currentRwtEligStartDate)) {
                return Optional.of(currentRwtEligYear);
            }
            //Check the next year...
            currentRwtEligStartDate = currentRwtEligStartDate.plusYears(1L);
            currentRwtEligYear++;
        }
        return Optional.empty();
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
            LOGGER.info("Listing: " + listing.getId() + " - Does not attest to any eligible criteria");
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
            LOGGER.info("Listing: " + listing.getId() + " - Certification date does not exist");
            return false;
        } else {
            LocalDate certDate = DateUtil.toLocalDate(listing.getCertificationDate());
            if (certDate.isBefore(eligibilityDate)) {
                return true;
            } else {
                LOGGER.info("Listing: " + listing.getId() + " - Certification date is after eligibility start date");
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
            LOGGER.info("Listing: " + listing.getId() + " - Not Active");
            return false;
        }
    }

    private Date convertLocalDateToDateUtcAtMidnight(LocalDate localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth(), 0, 0, 0);
        return calendar.getTime();
    }
}
