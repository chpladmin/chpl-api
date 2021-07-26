package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("realWorldTestingReviewer")
public class RealWorldTestingReviewer implements ComparisonReviewer {

    private static final String EDITION_2015 = "2015";

    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;


    @Autowired
    public RealWorldTestingReviewer(ValidationUtils validationUtils, ErrorMessageUtil errorMessageUtil) {
        this.validationUtils = validationUtils;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {

        //The rwtEligibilityYear cannot be updated
        if (!Objects.equals(existingListing.getRwtEligibilityYear(), updatedListing.getRwtEligibilityYear())) {
            updatedListing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.eligibilityYearNotUpdatable"));
            return;
        }

        //Always use the rwt elig year from the existing listing
        updatedListing.setRwtEligibilityYear(existingListing.getRwtEligibilityYear());

        if (isListingCurrentlyRwtEligible(existingListing)) {
            if (isRwtPlansDataSubmitted(updatedListing)) {
                validateRwtPlansUrl(updatedListing);
                validateRwtPlansCheckDate(updatedListing);

            }
            if (isRwtResultsDataSubmitted(updatedListing)) {
                validateRwtResultsUrl(updatedListing);
                validateRwtResultsCheckDate(updatedListing);
            }
        } else if (isRwtPlansDataSubmitted(updatedListing) || isRwtResultsDataSubmitted(updatedListing)) {
            updatedListing.getErrorMessages().add(errorMessageUtil.getMessage("listing.realWorldTesting.notEligible"));
        }
    }

    private boolean isListingCurrentlyRwtEligible(CertifiedProductSearchDetails listing) {
        return isListing2015Edition(listing);
    }

    @SuppressWarnings("checkstyle:linelength")
    private boolean isListing2015Edition(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY)) {
            return listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString().equals(EDITION_2015);
        }
        return false;
    }

    private void validateRwtPlansUrl(CertifiedProductSearchDetails listing) {
        if (StringUtils.isBlank(listing.getRwtPlansUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.required"));
        } else if (!validationUtils.isWellFormedUrl(listing.getRwtPlansUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.invalid"));
        }
    }

    private void validateRwtPlansCheckDate(CertifiedProductSearchDetails listing) {
        if (Objects.isNull(listing.getRwtPlansCheckDate())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plans.checkDate.required"));
        }
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

    private void validateRwtResultsCheckDate(CertifiedProductSearchDetails listing) {
        if (Objects.isNull(listing.getRwtResultsCheckDate())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.checkDate.required"));
        }
    }

    private boolean isRwtPlansDataSubmitted(CertifiedProductSearchDetails updatedListing) {
        return !StringUtils.isBlank(updatedListing.getRwtPlansUrl())
                || Objects.nonNull(updatedListing.getRwtPlansCheckDate());
    }

    private boolean isRwtResultsDataSubmitted(CertifiedProductSearchDetails updatedListing) {
        return !StringUtils.isBlank(updatedListing.getRwtResultsUrl())
                || Objects.nonNull(updatedListing.getRwtResultsCheckDate());
    }
}
