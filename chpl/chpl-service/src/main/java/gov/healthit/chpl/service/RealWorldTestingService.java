package gov.healthit.chpl.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
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

    @Value("${rwtEligibilityDayOfYear}")
    private String rwtEligibilityDayOfYear;

    private CertificationCriterionService certificationCriterionService;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Autowired
    public RealWorldTestingService(CertificationCriterionService certificationCriterionService) {
        this.certificationCriterionService = certificationCriterionService;
    }

    public boolean doesListingAttestToEligibleCriteria(CertifiedProductSearchDetails listing) {
        if (!isListingStatusActiveAsOfEligibilityDate(listing)) {
            LOGGER.info("Listing: " + listing.getId() + " - Not active as of " + dateFormatter.format(getMostRecentPastEligibilityDate()));
            return false;
        } else if (!isCertificationDateBeforeEligibilityDate(listing)) {
            LOGGER.info("Listing: " + listing.getId() + " - Certification date is not before Eligility Date");
            return false;
        } else {
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
    }

    public boolean doesListingAttestToEligibleCriteria(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> eligibleCriteria = getRwtEligibleCriteria();
        boolean doesExist = listing.getCertificationCriterion().stream()
                .filter(result -> result.getMeetsCriteria()
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

    private boolean isCertificationDateBeforeEligibilityDate(CertifiedProductSearchDetails listing) {
        if (Objects.isNull(listing) || Objects.isNull(listing.getCertificationDate())) {
            LOGGER.info("Listing: " + listing.getId() + " - Certification date does not exist");
            return false;
        } else {
            LocalDate certDate = DateUtil.toLocalDate(listing.getCertificationDate());
            if (certDate.isBefore(getMostRecentPastEligibilityDate())) {
                return true;
            } else {
                LOGGER.info("Listing: " + listing.getId() + " - Certification date is after eligibility start date");
                return false;
            }
        }
    }

    private boolean isListingStatusActiveAsOfEligibilityDate(CertifiedProductSearchDetails listing) {
        CertificationStatusEvent event = listing.getStatusOnDate(convertLocalDateToDateUtcAtMidnight(getMostRecentPastEligibilityDate()));
        if (Objects.nonNull(event)
                && event.getStatus().getName().equals(CertificationStatusType.Active.getName())) {
            return true;
        } else {
            LOGGER.info("Listing: " + listing.getId() + " - Not Active");
            return false;
        }
    }

    public LocalDate getMostRecentPastEligibilityDate() {
        LocalDate eligDateWithCurrentYear = LocalDate.from(dateFormatter.parse(rwtEligibilityDayOfYear + "/" + LocalDate.now().getYear()));
        if (eligDateWithCurrentYear.isBefore(LocalDate.now())) {
            return eligDateWithCurrentYear;
        } else {
            return eligDateWithCurrentYear.minusYears(1L);
        }
    }

    private Date convertLocalDateToDateUtcAtMidnight(LocalDate localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth(), 0, 0, 0);
        return calendar.getTime();
    }
}
