package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("realWorldTestingReviewer")
public class RealWorldTestingReviewer implements Reviewer {

    private static final String EDITION_2015 = "2015";

    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public RealWorldTestingReviewer(CertifiedProductDetailsManager certifiedProductDetailsManager,
            ValidationUtils validationUtils, ErrorMessageUtil errorMessageUtil) {

        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.validationUtils = validationUtils;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails updatedListing) {

        if (isListingCurrentlyRwtEligible(updatedListing)) {
            if (isRwtPlansDataSubmitted(updatedListing)) {
                validateRwtPlansUrl(updatedListing);
                validateRwtPlansCheckDate(updatedListing);

            }
            if (isRwtResultsDataSubmitted(updatedListing)) {
                validateRwtResultsUrl(updatedListing);
                validateRwtResultsCheckDate(updatedListing);
            }
        } else if (isRwtPlansDataSubmitted(updatedListing) || isRwtResultsDataSubmitted(updatedListing)) {
            updatedListing.addBusinessErrorMessage(errorMessageUtil.getMessage("listing.realWorldTesting.notEligible"));
        }
    }

    private boolean isListingCurrentlyRwtEligible(CertifiedProductSearchDetails listing) {
        return isListing2015Edition(listing);
    }

    private boolean isListing2015Edition(CertifiedProductSearchDetails listing) {
        try {
            CertifiedProductSearchDetails cpsd = certifiedProductDetailsManager.getCertifiedProductDetails(listing.getId());
            return cpsd.getEdition() == null || cpsd.getEdition().getName().equals(EDITION_2015);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not determine the edition of listing {}", listing.getId(), e);
            return false;
        }
    }

    private void validateRwtPlansUrl(CertifiedProductSearchDetails listing) {
        if (StringUtils.isBlank(listing.getRwtPlansUrl())) {
            listing.addBusinessErrorMessage(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.required"));
        } else if (!validationUtils.isWellFormedUrl(listing.getRwtPlansUrl())) {
            listing.addBusinessErrorMessage(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.invalid"));
        }
    }

    private void validateRwtPlansCheckDate(CertifiedProductSearchDetails listing) {
        if (Objects.isNull(listing.getRwtPlansCheckDate())) {
            listing.addBusinessErrorMessage(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plans.checkDate.required"));
        }
    }

    private void validateRwtResultsUrl(CertifiedProductSearchDetails listing) {
        if (StringUtils.isBlank(listing.getRwtResultsUrl())) {
            listing.addBusinessErrorMessage(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.url.required"));
        } else if (!validationUtils.isWellFormedUrl(listing.getRwtResultsUrl())) {
            listing.addBusinessErrorMessage(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.url.invalid"));
        }
    }

    private void validateRwtResultsCheckDate(CertifiedProductSearchDetails listing) {
        if (Objects.isNull(listing.getRwtResultsCheckDate())) {
            listing.addBusinessErrorMessage(
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
