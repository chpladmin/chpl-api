package gov.healthit.chpl.validation.listing.reviewer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("realWorldTestingReviewer")
public class RealWorldTestingReviewer implements ComparisonReviewer {

    private static final String EDITION_2015 = "2015";

    @Value("${rwtPlanStartDayOfYear}")
    private String rwtPlanStartDayOfYear;

    @Value("${rwtResultsStartDayOfYear}")
    private String rwtResultsStartDayOfYear;

    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Autowired
    public RealWorldTestingReviewer(ValidationUtils validationUtils, ErrorMessageUtil errorMessageUtil) {
        this.validationUtils = validationUtils;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        //Always use the rwt elig year from the existing listing
        updatedListing.setRwtEligibilityYear(existingListing.getRwtEligibilityYear());

        if (isListingCurrentlyRwtEligible(existingListing)) {
            if (isRwtPlanDataSubmitted(updatedListing)) {
                validateRwtPlanUrl(updatedListing);
                validateRwtPlanSubmissionDate(updatedListing);

            }
            if (isRwtResultsDataSubmitted(updatedListing)) {
                validateRwtResultsUrl(updatedListing);
                validateRwtResultsSubmissionDate(updatedListing);
            }
        } else if (isRwtPlanDataSubmitted(updatedListing) || isRwtResultsDataSubmitted(updatedListing)) {
            updatedListing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.notEligible"));
        }
    }

    private boolean isListingCurrentlyRwtEligible(CertifiedProductSearchDetails listing) {
        return Objects.nonNull(listing.getRwtEligibilityYear()) && isListing2015Edition(listing);
    }

    private boolean isListing2015Edition(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY)) {
            return listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString().equals(EDITION_2015);
        }
        return false;
    }

    private void validateRwtPlanUrl(CertifiedProductSearchDetails listing) {
        if (StringUtils.isBlank(listing.getRwtPlanUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plan.url.required"));
        } else if (!validationUtils.isWellFormedUrl(listing.getRwtPlanUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plan.url.invalid"));
        }
    }

    private void validateRwtPlanSubmissionDate(CertifiedProductSearchDetails listing) {
        if (Objects.isNull(listing.getRwtPlanSubmissionDate())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plan.submissionDate.required"));
        } else if (isRwtPlanDateBeforePlanEligibleDate(listing)) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plan.submissionDate.invalid",
                            getPlanEligibleDate(listing).format(dateFormatter)));
        }
    }

    private boolean isRwtPlanDateBeforePlanEligibleDate(CertifiedProductSearchDetails listing) {
        return listing.getRwtPlanSubmissionDate().isBefore(getPlanEligibleDate(listing));
    }

    private LocalDate getPlanEligibleDate(CertifiedProductSearchDetails listing) {
        return getLocalDate(rwtPlanStartDayOfYear, listing.getRwtEligibilityYear() - 1);
    }


    private void validateRwtResultsUrl(CertifiedProductSearchDetails listing) {
        if (StringUtils.isBlank(listing.getRwtResultsUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.url.required"));
        } else if (!validationUtils.isWellFormedUrl(listing.getRwtResultsUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.url.invalid"));
        }
    }

    private void validateRwtResultsSubmissionDate(CertifiedProductSearchDetails listing) {
        if (Objects.isNull(listing.getRwtResultsSubmissionDate())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.submissionDate.required"));
        } else if (isRwtResultsDateBeforeResultsEligibleDate(listing)) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.submissionDate.invalid",
                            getResultsEligibleDate(listing).format(dateFormatter)));
        }
    }

    private boolean isRwtResultsDateBeforeResultsEligibleDate(CertifiedProductSearchDetails listing) {
        return listing.getRwtResultsSubmissionDate().isBefore(getResultsEligibleDate(listing));
    }

    private LocalDate getResultsEligibleDate(CertifiedProductSearchDetails listing) {
        return getLocalDate(rwtResultsStartDayOfYear, listing.getRwtEligibilityYear() + 1);
    }


    private boolean isRwtPlanDataSubmitted(CertifiedProductSearchDetails updatedListing) {
        return !StringUtils.isBlank(updatedListing.getRwtPlanUrl())
                || Objects.nonNull(updatedListing.getRwtPlanSubmissionDate());
    }

    private boolean isRwtResultsDataSubmitted(CertifiedProductSearchDetails updatedListing) {
        return !StringUtils.isBlank(updatedListing.getRwtResultsUrl())
                || Objects.nonNull(updatedListing.getRwtResultsSubmissionDate());
    }


    private LocalDate toLocalDate(Date dateToConvert) {
        if (Objects.nonNull(dateToConvert)) {
            return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    private LocalDate getLocalDate(String dayAndMonth, Integer year) {
        // dayOfYear s/b in MM/dd format
        return LocalDate.parse(dayAndMonth + "/" + year.toString(), dateFormatter);
    }
}
