package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("realWorldTestingReviewer")
public class RealWorldTestingReviewer implements ComparisonReviewer {

    @Value("${rwtPlanStartDayOfYear}")
    private String rwtPlanStartDayOfYear;

    @Value("${rwtPlanRequiredDateOfYear}")
    private String rwtPlanRequiredDateOfYear;

    @Value("${rwtResultsStartDayOfYear}")
    private String rwtResultsStartDayOfYear;

    @Value("${rwtResultsRequiredDateOfYear}")
    private String rwtResultsRequiredDateOfYear;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        if (isListingCurrentlyRwtPlanEligible(existingListing)) {
            LOGGER.info("The listing is currently RWT Plan Eligible.");
            if (isRwtPlanDataSubmitted(updatedListing)) {
                if (!isUrlValid(updatedListing.getRwtPlanUrl())) {
                    updatedListing.getErrorMessages().add("Plan URL is required.");
                } else if (!isDateValid(toLocalDate(updatedListing.getRwtPlanSubmissionDate()))) {
                    updatedListing.getErrorMessages().add("Plan Submission Confirmed Date is required.");
                }
            } else if (isListingCurrentlyRwtPlanPastDue(existingListing)) {
                updatedListing.getWarningMessages().add("Real World Testing Plan required for this listing");
            }

        } else if (isRwtPlanDataSubmitted(updatedListing)) {
            updatedListing.getErrorMessages().add("Listing is not eligible for Real World Testing");
        }

        if (isListingCurrentlyRwtResultsEligible(existingListing)) {
            LOGGER.info("The listing is currently RWT Plan Eligible.");
            if (isRwtResultsDataSubmitted(updatedListing)) {
                if (!isUrlValid(updatedListing.getRwtPlanUrl())) {
                    updatedListing.getErrorMessages().add("Results URL is required.");
                } else if (!isDateValid(toLocalDate(updatedListing.getRwtPlanSubmissionDate()))) {
                    updatedListing.getErrorMessages().add("Results Submission Confirmed Date is required.");
                }
            } else if (isListingCurrentlyRwtResultsPastDue(existingListing)) {
                updatedListing.getWarningMessages().add("Real World Testing Results required for this listing");
            }
        } else if (isRwtResultsDataSubmitted(updatedListing)) {
            updatedListing.getErrorMessages().add("Listing is not eligible for Real World Testing");
        }
    }

    //TODO: Need to figure out how we are validating URLs
    private boolean isUrlValid(String url) {
        return !StringUtils.isBlank(url);
    }

    //TODO: Need to determine if we are doing any validation here (other than it being a valida date
    private boolean isDateValid(LocalDate date) {
        return Objects.nonNull(date);
    }

    private boolean isRwtPlanDataSubmitted(CertifiedProductSearchDetails updatedListing) {
        return !StringUtils.isAllBlank(updatedListing.getRwtPlanUrl())
                || Objects.nonNull(updatedListing.getRwtPlanSubmissionDate());
    }

    private boolean isRwtResultsDataSubmitted(CertifiedProductSearchDetails updatedListing) {
        return !StringUtils.isAllBlank(updatedListing.getRwtResultsUrl())
                || Objects.nonNull(updatedListing.getRwtResultsSubmissionDate());
    }

    private boolean isListingCurrentlyRwtPlanEligible(CertifiedProductSearchDetails existingListing) {
        if (Objects.nonNull(existingListing.getRwtEligibilityYear())) {
            Integer calculatedYearBasedOnEligYear = existingListing.getRwtEligibilityYear() - 1;
            LocalDate rwtPlanEligibilityStartDate =
                    LocalDate.parse(rwtPlanStartDayOfYear + "/" + calculatedYearBasedOnEligYear.toString(), dateFormatter);
            return LocalDate.now().isAfter(rwtPlanEligibilityStartDate);
        }
        return false;
    }

    private boolean isListingCurrentlyRwtResultsEligible(CertifiedProductSearchDetails existingListing) {
        if (Objects.nonNull(existingListing.getRwtEligibilityYear())) {
            Integer calculatedYearBasedOnEligYear = existingListing.getRwtEligibilityYear() + 1;
            LocalDate rwtResultsEligibilityStartDate =
                    LocalDate.parse(rwtResultsStartDayOfYear + "/" + calculatedYearBasedOnEligYear.toString(), dateFormatter);
            return LocalDate.now().isAfter(rwtResultsEligibilityStartDate);
        }
        return false;
    }

    private boolean isListingCurrentlyRwtPlanPastDue(CertifiedProductSearchDetails existingListing) {
        if (Objects.nonNull(existingListing.getRwtEligibilityYear())) {
            Integer calculatedYearBasedOnEligYear = existingListing.getRwtEligibilityYear() - 1;
            LocalDate rwtPlanEligibilityPastDueDate =
                    LocalDate.parse(rwtPlanRequiredDateOfYear + "/" + calculatedYearBasedOnEligYear.toString(), dateFormatter);
            return LocalDate.now().isAfter(rwtPlanEligibilityPastDueDate);
        }
        return false;
    }


    private boolean isListingCurrentlyRwtResultsPastDue(CertifiedProductSearchDetails existingListing) {
        if (Objects.nonNull(existingListing.getRwtEligibilityYear())) {
            Integer calculatedYearBasedOnEligYear = existingListing.getRwtEligibilityYear() + 1;
            LocalDate rwtResultsEligibilityPastDueDate =
                    LocalDate.parse(rwtResultsRequiredDateOfYear + "/" + calculatedYearBasedOnEligYear.toString(), dateFormatter);
            return LocalDate.now().isAfter(rwtResultsEligibilityPastDueDate);
        }
        return false;
    }

    private LocalDate toLocalDate(Date dateToConvert) {
        if (Objects.nonNull(dateToConvert)) {
            return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }
}
