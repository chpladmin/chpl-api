package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("listingUploadAdditionalSoftwareFrameworkReviewer")
public class AdditionalSoftwareReviewer extends PermissionBasedReviewer {
    private CertificationResultRules certResultRules;

    @Autowired
    public AdditionalSoftwareReviewer(CertificationResultRules certResultRules,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.certResultRules = certResultRules;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                && BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveAdditionalSoftware(listing, certResult);
        reviewAdditionalSoftwareListMatchesAdditionalSoftwareBoolean(listing, certResult);
        reviewAdditionalSoftwareHasEitherNameOrListing(listing, certResult);
        reviewAdditionalSoftwareListingsAreValid(listing, certResult);
    }

    private void reviewCriteriaCanHaveAdditionalSoftware(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)
                && certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.additionalSoftwareFrameworkNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewAdditionalSoftwareListMatchesAdditionalSoftwareBoolean(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getHasAdditionalSoftware() != null && certResult.getHasAdditionalSoftware()
                && (certResult.getAdditionalSoftware() == null || certResult.getAdditionalSoftware().size() == 0)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.noAdditionalSoftwareMismatch",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else  if (certResult.getHasAdditionalSoftware() != null && !certResult.getHasAdditionalSoftware()
                && certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.hasAdditionalSoftwareMismatch",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewAdditionalSoftwareHasEitherNameOrListing(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0) {
            certResult.getAdditionalSoftware().stream()
                .filter(additionalSoftware -> areNameAndListingFilledIn(additionalSoftware))
                .forEach(additionalSoftware -> listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.additionalSoftwareHasNameAndListingData",
                                Util.formatCriteriaNumber(certResult.getCriterion()))));
        }
    }

    private boolean areNameAndListingFilledIn(CertificationResultAdditionalSoftware additionalSoftware) {
        return !StringUtils.isEmpty(additionalSoftware.getCertifiedProductNumber())
                && (!StringUtils.isEmpty(additionalSoftware.getName()) || !StringUtils.isEmpty(additionalSoftware.getVersion()));
    }

    private void reviewAdditionalSoftwareListingsAreValid(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0) {
            certResult.getAdditionalSoftware().stream()
                .filter(additionalSoftware -> !StringUtils.isEmpty(additionalSoftware.getCertifiedProductNumber()) && additionalSoftware.getCertifiedProductId() == null)
                .forEach(additionalSoftware -> addCriterionErrorOrWarningByPermission(listing, certResult,
                                                    "listing.criteria.invalidAdditionalSoftware",
                                                    additionalSoftware.getCertifiedProductNumber(),
                                                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}
