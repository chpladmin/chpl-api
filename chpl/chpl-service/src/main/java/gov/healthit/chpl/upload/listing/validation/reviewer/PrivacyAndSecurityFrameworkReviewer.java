package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("listingUploadPrivacyAndSecurityFrameworkReviewer")
public class PrivacyAndSecurityFrameworkReviewer  implements Reviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterion b11;

    @Autowired
    public PrivacyAndSecurityFrameworkReviewer(CertificationResultRules certResultRules,
            CertificationCriterionService criteriaService,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
        this.b11 = criteriaService.get(Criteria2015.B_11);
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> review(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removePrivacyAndSecurityFrameworkIfNotApplicable(certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHavePrivacyAndSecurity(listing, certResult);
        reviewPrivacyAndSecurityRequired(listing, certResult);
        reviewPrivacyAndSecurityValid(listing, certResult);
    }

    private void reviewCriteriaCanHavePrivacyAndSecurity(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.PRIVACY_SECURITY)) {
            if (!StringUtils.isEmpty(certResult.getPrivacySecurityFramework())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.privacyAndSecurityFrameworkNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setPrivacySecurityFramework(null);
        }
    }

    private void removePrivacyAndSecurityFrameworkIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        }
    }

    private void reviewPrivacyAndSecurityRequired(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.PRIVACY_SECURITY)
                && !b11.getId().equals(certResult.getCriterion().getId())
                && StringUtils.isEmpty(certResult.getPrivacySecurityFramework())) {
            listing.addDataErrorMessage(msgUtil.getMessage(
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
                listing.addDataErrorMessage(msgUtil.getMessage(
                        "listing.criteria.invalidPrivacySecurityFramework",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        formattedPrivacyAndSecurityFramework,
                        PrivacyAndSecurityFrameworkConcept.getFormattedValues()));
            }
        }
    }
}
