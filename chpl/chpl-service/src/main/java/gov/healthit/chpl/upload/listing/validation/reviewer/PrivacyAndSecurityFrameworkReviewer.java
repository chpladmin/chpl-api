package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadPrivacyAndSecurityFrameworkReviewer")
public class PrivacyAndSecurityFrameworkReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public PrivacyAndSecurityFrameworkReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> review(listing, certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHavePrivacyAndSecurity(listing, certResult);
        reviewPrivacyAndSecurityRequired(listing, certResult);
        reviewPrivacyAndSecurityValid(listing, certResult);
    }

    private void reviewCriteriaCanHavePrivacyAndSecurity(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
            if (!StringUtils.isEmpty(certResult.getPrivacySecurityFramework())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.privacyAndSecurityFrameworkNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setPrivacySecurityFramework(null);
        }
    }

    private void reviewPrivacyAndSecurityRequired(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                && StringUtils.isEmpty(certResult.getPrivacySecurityFramework())) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.missingPrivacySecurityFramework",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewPrivacyAndSecurityValid(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!StringUtils.isEmpty(certResult.getPrivacySecurityFramework())) {
            String formattedPrivacyAndSecurityFramework = CertificationResult
                    .formatPrivacyAndSecurityFramework(certResult.getPrivacySecurityFramework());
            PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept
                    .getValue(formattedPrivacyAndSecurityFramework);
            if (foundPrivacyAndSecurityFramework == null) {
                listing.getErrorMessages().add(msgUtil.getMessage(
                        "listing.criteria.invalidPrivacySecurityFramework",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        formattedPrivacyAndSecurityFramework,
                        PrivacyAndSecurityFrameworkConcept.getFormattedValues()));
            }
        }
    }
}
