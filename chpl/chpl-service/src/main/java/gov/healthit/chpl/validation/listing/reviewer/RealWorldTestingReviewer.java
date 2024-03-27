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
public class RealWorldTestingReviewer implements Reviewer {

    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public RealWorldTestingReviewer(ValidationUtils validationUtils, ErrorMessageUtil errorMessageUtil) {
        this.validationUtils = validationUtils;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails updatedListing) {
        if (isRwtPlansDataSubmitted(updatedListing)) {
            validateRwtPlansUrl(updatedListing);
            validateRwtPlansCheckDate(updatedListing);

        }
        if (isRwtResultsDataSubmitted(updatedListing)) {
            validateRwtResultsUrl(updatedListing);
            validateRwtResultsCheckDate(updatedListing);
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
        if (Objects.isNull(listing.getRwtPlansCheckDate())
                && !StringUtils.isEmpty(listing.getUserEnteredRwtPlansCheckDate())) {
            listing.addBusinessErrorMessage(
                    errorMessageUtil.getMessage("listing.realWorldTesting.plans.checkDate.invalid", listing.getUserEnteredRwtPlansCheckDate()));
        } else if (Objects.isNull(listing.getRwtPlansCheckDate())) {
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
        if (Objects.isNull(listing.getRwtResultsCheckDate())
                && !StringUtils.isEmpty(listing.getUserEnteredRwtResultsCheckDate())) {
            listing.addBusinessErrorMessage(
                    errorMessageUtil.getMessage("listing.realWorldTesting.results.checkDate.invalid", listing.getUserEnteredRwtResultsCheckDate()));
        } else if (Objects.isNull(listing.getRwtResultsCheckDate())) {
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
