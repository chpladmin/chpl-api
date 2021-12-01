package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.GapAllowedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;

@Component
public class CertificationResultReviewer {
    private CriteriaReviewer criteriaReviewer;
    private PrivacyAndSecurityFrameworkReviewer privacyAndSecurityFrameworkReviewer;
    private AdditionalSoftwareReviewer additionalSoftwareReviewer;
    private GapAllowedReviewer gapAllowedReviewer;
    private TestToolReviewer testToolReviewer;
    private TestDataReviewer testDataReviewer;
    private TestProcedureReviewer testProcedureReviewer;
    private TestFunctionalityReviewer testFunctionalityReviewer;
    private TestStandardReviewer testStandardReviewer;
    private OptionalStandardReviewer optionalStandardReviewer;
    private SvapReviewer svapReviewer;
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    private OldCriteriaWithoutIcsReviewer oldCriteriaWithoutIcsReviewer;
    private SedG32015Reviewer sedG3Reviewer;
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public CertificationResultReviewer(@Qualifier("listingUploadCriteriaReviewer") CriteriaReviewer criteriaReviewer,
            @Qualifier("listingUploadPrivacyAndSecurityFrameworkReviewer") PrivacyAndSecurityFrameworkReviewer privacyAndSecurityFrameworkReviewer,
            @Qualifier("listingUploadAdditionalSoftwareFrameworkReviewer") AdditionalSoftwareReviewer additionalSoftwareReviewer,
            @Qualifier("gapAllowedReviewer") GapAllowedReviewer gapAllowedReviewer,
            @Qualifier("listingUploadTestToolReviewer") TestToolReviewer testToolReviewer,
            @Qualifier("listingUploadTestDataReviewer") TestDataReviewer testDataReviewer,
            @Qualifier("listingUploadTestProcedureReviewer") TestProcedureReviewer testProcedureReviewer,
            @Qualifier("listingUploadTestFunctionalityReviewer") TestFunctionalityReviewer testFunctionalityReviewer,
            @Qualifier("listingUploadTestStandardReviewer") TestStandardReviewer testStandardReviewer,
            @Qualifier("listingUploadOptionalStandardReviewer") OptionalStandardReviewer optionalStandardReviewer,
            @Qualifier("listingUploadSvapReviewer") SvapReviewer svapReviewer,
            @Qualifier("uploadedListingUnattestedCriteriaWithDataReviewer") UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer,
            @Qualifier("oldCriteriaWithoutIcsReviewer") OldCriteriaWithoutIcsReviewer oldCriteriaWithoutIcsReviewer,
            @Qualifier("sedG32015Reviewer") SedG32015Reviewer sedG3Reviewer,
            CertificationResultRules certResultRules, ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.criteriaReviewer = criteriaReviewer;
        this.privacyAndSecurityFrameworkReviewer = privacyAndSecurityFrameworkReviewer;
        this.additionalSoftwareReviewer = additionalSoftwareReviewer;
        this.gapAllowedReviewer = gapAllowedReviewer;
        this.testToolReviewer = testToolReviewer;
        this.testDataReviewer = testDataReviewer;
        this.testProcedureReviewer = testProcedureReviewer;
        this.testFunctionalityReviewer = testFunctionalityReviewer;
        this.testStandardReviewer = testStandardReviewer;
        this.optionalStandardReviewer = optionalStandardReviewer;
        this.svapReviewer = svapReviewer;
        this.unattestedCriteriaWithDataReviewer = unattestedCriteriaWithDataReviewer;
        this.oldCriteriaWithoutIcsReviewer = oldCriteriaWithoutIcsReviewer;
        this.sedG3Reviewer = sedG3Reviewer;
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationResults"));
            return;
        }

        listing.getCertificationResults().stream()
            .forEach(certResult -> reviewSuccessField(listing, certResult));

        listing.getCertificationResults().stream()
            .filter(certResult -> certResult != null && certResult.getCriterion() != null
                        && certResult.getCriterion().getId() != null
                        && validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> reviewCertResultFields(listing, certResult));
        criteriaReviewer.review(listing);
        privacyAndSecurityFrameworkReviewer.review(listing);
        additionalSoftwareReviewer.review(listing);
        gapAllowedReviewer.review(listing);
        testToolReviewer.review(listing);
        testDataReviewer.review(listing);
        testProcedureReviewer.review(listing);
        testFunctionalityReviewer.review(listing);
        testStandardReviewer.review(listing);
        optionalStandardReviewer.review(listing);
        svapReviewer.review(listing);
        unattestedCriteriaWithDataReviewer.review(listing);
        oldCriteriaWithoutIcsReviewer.review(listing);
        sedG3Reviewer.review(listing);
    }

    private void reviewSuccessField(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSuccess() == null && !StringUtils.isEmpty(certResult.getSuccessStr())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.invalidSuccess",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    certResult.getSuccessStr()));
        }
    }

    private void reviewCertResultFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewGap(listing, certResult);
        reviewAdditionalSoftwareString(listing, certResult);
        reviewAttestationAnswer(listing, certResult);
        reviewApiDocumentation(listing, certResult);
        reviewExportDocumentation(listing, certResult);
        reviewUseCases(listing, certResult);
        reviewServiceBaseUrlList(listing, certResult);
    }

    private void reviewGap(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.GAP)
                && certResult.isGap() == null) {
            if (!StringUtils.isEmpty(certResult.getGapStr())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.invalidGap",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        certResult.getGapStr()));
            } else {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingGap",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.GAP)) {
            if (certResult.isGap() != null | !StringUtils.isEmpty(certResult.getGapStr())) {
                listing.getWarningMessages().add(
                        msgUtil.getMessage("listing.criteria.gapNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setGap(null);
        }
    }

    private void reviewAdditionalSoftwareString(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!StringUtils.isEmpty(certResult.getHasAdditionalSoftwareStr())
                && certResult.getHasAdditionalSoftware() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.invalidHasAdditionalSoftware",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    certResult.getHasAdditionalSoftwareStr()));

        }
    }

    private void reviewAttestationAnswer(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)
                && certResult.getAttestationAnswer() == null) {
            if (!StringUtils.isEmpty(certResult.getAttestationAnswerStr())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.invalidAttestationAnswer",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        certResult.getAttestationAnswerStr()));

            } else {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingAttestationAnswer",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)) {
            if (!StringUtils.isEmpty(certResult.getAttestationAnswer())
                    || !StringUtils.isEmpty(certResult.getAttestationAnswerStr())) {
                listing.getWarningMessages().add(
                        msgUtil.getMessage("listing.criteria.attestationAnswerNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setAttestationAnswer(null);
        }
    }

    private void reviewApiDocumentation(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)
                && StringUtils.isEmpty(certResult.getApiDocumentation())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingApiDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
            if (!StringUtils.isEmpty(certResult.getApiDocumentation())) {
                listing.getWarningMessages().add(
                        msgUtil.getMessage("listing.criteria.apiDocumentationNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setApiDocumentation(null);
        }
    }

    private void reviewExportDocumentation(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                && StringUtils.isEmpty(certResult.getExportDocumentation())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingExportDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
            if (!StringUtils.isEmpty(certResult.getExportDocumentation())) {
                listing.getWarningMessages().add(
                        msgUtil.getMessage("listing.criteria.exportDocumentationNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setExportDocumentation(null);
        }
    }

    private void reviewUseCases(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                && StringUtils.isEmpty(certResult.getUseCases())
                && certResult.getAttestationAnswer() != null && certResult.getAttestationAnswer().equals(Boolean.TRUE)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingUseCases",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                && !StringUtils.isEmpty(certResult.getUseCases())
                && (certResult.getAttestationAnswer() == null || certResult.getAttestationAnswer().equals(Boolean.FALSE))) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                            Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.USE_CASES)) {
            if (!StringUtils.isEmpty(certResult.getUseCases())) {
                listing.getWarningMessages().add(
                        msgUtil.getMessage("listing.criteria.useCasesNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setUseCases(null);
        }
    }

    private void reviewServiceBaseUrlList(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                && StringUtils.isEmpty(certResult.getServiceBaseUrlList())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingServiceBaseUrlList",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.SERVICE_BASE_URL_LIST)) {
            if (!StringUtils.isEmpty(certResult.getServiceBaseUrlList())) {
                listing.getWarningMessages().add(
                        msgUtil.getMessage("listing.criteria.serviceBaseUrlListNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setServiceBaseUrlList(null);
        }
    }
}
