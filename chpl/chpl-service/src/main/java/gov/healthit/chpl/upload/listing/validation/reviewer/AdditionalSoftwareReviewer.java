package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadAdditionalSoftwareFrameworkReviewer")
public class AdditionalSoftwareReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AdditionalSoftwareReviewer(CertificationResultRules certResultRules, ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                        && validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> review(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeAdditionalSoftwareIfNotApplicable(certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveAdditionalSoftware(listing, certResult);
        reviewAdditionalSoftwareListMatchesAdditionalSoftwareBoolean(listing, certResult);
        reviewAdditionalSoftwareHasEitherNameOrListing(listing, certResult);
        reviewAdditionalSoftwareListingsAreValid(listing, certResult);
    }

    private void reviewCriteriaCanHaveAdditionalSoftware(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            if (!CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.additionalSoftwareNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setAdditionalSoftware(null);
        }
    }

    private void removeAdditionalSoftwareIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            certResult.setAdditionalSoftware(null);
        }
    }

    private void reviewAdditionalSoftwareListMatchesAdditionalSoftwareBoolean(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getHasAdditionalSoftware() != null && certResult.getHasAdditionalSoftware()
                && CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.noAdditionalSoftwareMismatch",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (certResult.getHasAdditionalSoftware() != null && !certResult.getHasAdditionalSoftware()
                && !CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.hasAdditionalSoftwareMismatch",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewAdditionalSoftwareHasEitherNameOrListing(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
            certResult.getAdditionalSoftware().stream()
                    .filter(additionalSoftware -> areNameAndListingFilledIn(additionalSoftware))
                    .forEach(additionalSoftware -> listing.addDataErrorMessage(
                            msgUtil.getMessage("listing.criteria.additionalSoftwareHasNameAndListingData",
                                    Util.formatCriteriaNumber(certResult.getCriterion()))));
        }
    }

    private boolean areNameAndListingFilledIn(CertificationResultAdditionalSoftware additionalSoftware) {
        return !StringUtils.isEmpty(additionalSoftware.getCertifiedProductNumber())
                && (!StringUtils.isEmpty(additionalSoftware.getName()) || !StringUtils.isEmpty(additionalSoftware.getVersion()));
    }

    private void reviewAdditionalSoftwareListingsAreValid(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
            certResult.getAdditionalSoftware().stream()
                    .filter(additionalSoftware -> !StringUtils.isEmpty(additionalSoftware.getCertifiedProductNumber()) && additionalSoftware.getCertifiedProductId() == null)
                    .forEach(additionalSoftware -> listing.addDataErrorMessage(msgUtil.getMessage(
                            "listing.criteria.invalidAdditionalSoftware",
                            additionalSoftware.getCertifiedProductNumber(),
                            Util.formatCriteriaNumber(certResult.getCriterion()))));
        }
    }
}
