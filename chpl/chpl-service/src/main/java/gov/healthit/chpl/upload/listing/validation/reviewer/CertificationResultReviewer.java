package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.ConformanceMethodReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.GapAllowedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;

@Component
public class CertificationResultReviewer {
    private UnavailableCriteriaReviewer unavailableCriteriaReviewer;
    private CriteriaReviewer criteriaReviewer;
    private PrivacyAndSecurityFrameworkReviewer privacyAndSecurityFrameworkReviewer;
    private AdditionalSoftwareReviewer additionalSoftwareReviewer;
    private GapAllowedReviewer gapAllowedReviewer;
    private TestToolReviewer testToolReviewer;
    private TestDataReviewer testDataReviewer;
    private ConformanceMethodReviewer conformanceMethodReviewer;
    private FunctionalityTestedReviewer functionalityTestedReviewer;
    private StandardReviewer standardReviewer;
    private TestStandardReviewer testStandardReviewer;
    private OptionalStandardReviewer optionalStandardReviewer;
    private SvapReviewer svapReviewer;
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    private SedG32015Reviewer sedG3Reviewer;
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public CertificationResultReviewer(@Qualifier("listingUploadUnavailableCriteriaReviewer") UnavailableCriteriaReviewer unavailableCriteriaReviewer,
            @Qualifier("listingUploadCriteriaReviewer") CriteriaReviewer criteriaReviewer,
            @Qualifier("listingUploadPrivacyAndSecurityFrameworkReviewer") PrivacyAndSecurityFrameworkReviewer privacyAndSecurityFrameworkReviewer,
            @Qualifier("listingUploadAdditionalSoftwareFrameworkReviewer") AdditionalSoftwareReviewer additionalSoftwareReviewer,
            @Qualifier("gapAllowedReviewer") GapAllowedReviewer gapAllowedReviewer,
            @Qualifier("listingUploadTestToolReviewer") TestToolReviewer testToolReviewer,
            @Qualifier("listingUploadTestDataReviewer") TestDataReviewer testDataReviewer,
            @Qualifier("conformanceMethodReviewer") ConformanceMethodReviewer conformanceMethodReviewer,
            @Qualifier("listingUploadFunctionalityTestedReviewer") FunctionalityTestedReviewer functionalityTestedReviewer,
            @Qualifier("listingUploadStandardReviewer") StandardReviewer standardReviewer,
            @Qualifier("listingUploadTestStandardReviewer") TestStandardReviewer testStandardReviewer,
            @Qualifier("listingUploadOptionalStandardReviewer") OptionalStandardReviewer optionalStandardReviewer,
            @Qualifier("listingUploadSvapReviewer") SvapReviewer svapReviewer,
            @Qualifier("uploadedListingUnattestedCriteriaWithDataReviewer") UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer,
            @Qualifier("sedG32015Reviewer") SedG32015Reviewer sedG3Reviewer,
            CertificationResultRules certResultRules, ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.unavailableCriteriaReviewer = unavailableCriteriaReviewer;
        this.criteriaReviewer = criteriaReviewer;
        this.privacyAndSecurityFrameworkReviewer = privacyAndSecurityFrameworkReviewer;
        this.additionalSoftwareReviewer = additionalSoftwareReviewer;
        this.gapAllowedReviewer = gapAllowedReviewer;
        this.testToolReviewer = testToolReviewer;
        this.testDataReviewer = testDataReviewer;
        this.conformanceMethodReviewer = conformanceMethodReviewer;
        this.functionalityTestedReviewer = functionalityTestedReviewer;
        this.standardReviewer = standardReviewer;
        this.testStandardReviewer = testStandardReviewer;
        this.optionalStandardReviewer = optionalStandardReviewer;
        this.svapReviewer = svapReviewer;
        this.unattestedCriteriaWithDataReviewer = unattestedCriteriaWithDataReviewer;
        this.sedG3Reviewer = sedG3Reviewer;
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (CollectionUtils.isEmpty(listing.getCertificationResults())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingCertificationResults"));
            return;
        }

        listing.getCertificationResults().stream()
                .filter(certResult -> certResult != null && certResult.getCriterion() != null)
                .forEach(certResult -> reviewUnconditionalFields(listing, certResult));

        listing.getCertificationResults().stream()
                .filter(certResult -> certResult != null && certResult.getCriterion() != null
                        && certResult.getCriterion().getId() != null
                        && validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> reviewCertResultFields(listing, certResult));

        listing.getCertificationResults().stream()
                .forEach(certResult -> removeCertResultFieldsNotApplicable(certResult));

        criteriaReviewer.review(listing);
        privacyAndSecurityFrameworkReviewer.review(listing);
        additionalSoftwareReviewer.review(listing);
        gapAllowedReviewer.review(listing);
        conformanceMethodReviewer.review(listing);
        testToolReviewer.review(listing);
        testDataReviewer.review(listing);
        functionalityTestedReviewer.review(listing);
        standardReviewer.review(listing);
        testStandardReviewer.review(listing);
        optionalStandardReviewer.review(listing);
        svapReviewer.review(listing);
        sedG3Reviewer.review(listing);
        unattestedCriteriaWithDataReviewer.review(listing);
    }

    private void reviewUnconditionalFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        unavailableCriteriaReviewer.review(listing, certResult);
        reviewSuccessField(listing, certResult);
    }

    private void reviewSuccessField(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getSuccess() == null && !ObjectUtils.isEmpty(certResult.getSuccessStr())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidSuccess",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    certResult.getSuccessStr()));
        }
    }

    private void removeCertResultFieldsNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.GAP)) {
            certResult.setGap(null);
        }
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.ATTESTATION_ANSWER)) {
            certResult.setAttestationAnswer(null);
        }
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        }
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
            certResult.setExportDocumentation(null);
        }
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.USE_CASES)) {
            certResult.setUseCases(null);
        }
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SERVICE_BASE_URL_LIST)) {
            certResult.setServiceBaseUrlList(null);
        }
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.RISK_MANAGEMENT_SUMMARY_INFORMATION)) {
            certResult.setRiskManagementSummaryInformation(null);
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
        reviewRiskManagementSummaryInformation(listing, certResult);
    }

    private void reviewGap(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.GAP)
                && certResult.getGap() == null) {
            if (!ObjectUtils.isEmpty(certResult.getGapStr())) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidGap",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        certResult.getGapStr()));
            } else {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingGap",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.GAP)) {
            if (certResult.getGap() != null | !ObjectUtils.isEmpty(certResult.getGapStr())) {
                listing.addWarningMessage(
                        msgUtil.getMessage("listing.criteria.gapNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setGap(null);
        }
    }

    private void reviewAdditionalSoftwareString(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!ObjectUtils.isEmpty(certResult.getHasAdditionalSoftwareStr())
                && certResult.getHasAdditionalSoftware() == null) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidHasAdditionalSoftware",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    certResult.getHasAdditionalSoftwareStr()));
        }
    }

    private void reviewAttestationAnswer(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.ATTESTATION_ANSWER)
                && certResult.getAttestationAnswer() == null) {
            if (!ObjectUtils.isEmpty(certResult.getAttestationAnswerStr())) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidAttestationAnswer",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        certResult.getAttestationAnswerStr()));

            } else {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingAttestationAnswer",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.ATTESTATION_ANSWER)) {
            if (!ObjectUtils.isEmpty(certResult.getAttestationAnswer())
                    || !ObjectUtils.isEmpty(certResult.getAttestationAnswerStr())) {
                listing.addWarningMessage(
                        msgUtil.getMessage("listing.criteria.attestationAnswerNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setAttestationAnswer(null);
        }
    }

    private void reviewApiDocumentation(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.API_DOCUMENTATION)
                && ObjectUtils.isEmpty(certResult.getApiDocumentation())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingApiDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.API_DOCUMENTATION)) {
            if (!ObjectUtils.isEmpty(certResult.getApiDocumentation())) {
                listing.addWarningMessage(
                        msgUtil.getMessage("listing.criteria.apiDocumentationNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setApiDocumentation(null);
        }
    }

    private void reviewExportDocumentation(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.EXPORT_DOCUMENTATION)
                && ObjectUtils.isEmpty(certResult.getExportDocumentation())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingExportDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
            if (!ObjectUtils.isEmpty(certResult.getExportDocumentation())) {
                listing.addWarningMessage(
                        msgUtil.getMessage("listing.criteria.exportDocumentationNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setExportDocumentation(null);
        }
    }

    private void reviewUseCases(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.USE_CASES)
                && ObjectUtils.isEmpty(certResult.getUseCases())
                && certResult.getAttestationAnswer() != null && certResult.getAttestationAnswer().equals(Boolean.TRUE)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingUseCases",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.USE_CASES)
                && !ObjectUtils.isEmpty(certResult.getUseCases())
                && (certResult.getAttestationAnswer() == null || certResult.getAttestationAnswer().equals(Boolean.FALSE))) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                            Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.USE_CASES)) {
            if (!ObjectUtils.isEmpty(certResult.getUseCases())) {
                listing.addWarningMessage(
                        msgUtil.getMessage("listing.criteria.useCasesNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setUseCases(null);
        }
    }

    private void reviewServiceBaseUrlList(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                && ObjectUtils.isEmpty(certResult.getServiceBaseUrlList())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingServiceBaseUrlList",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SERVICE_BASE_URL_LIST)) {
            if (!ObjectUtils.isEmpty(certResult.getServiceBaseUrlList())) {
                listing.addWarningMessage(
                        msgUtil.getMessage("listing.criteria.serviceBaseUrlListNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setServiceBaseUrlList(null);
        }
    }

    private void reviewRiskManagementSummaryInformation(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.RISK_MANAGEMENT_SUMMARY_INFORMATION)
                && ObjectUtils.isEmpty(certResult.getRiskManagementSummaryInformation())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingRiskManagementSummaryInformation",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.RISK_MANAGEMENT_SUMMARY_INFORMATION)) {
            if (!ObjectUtils.isEmpty(certResult.getRiskManagementSummaryInformation())) {
                listing.addWarningMessage(
                        msgUtil.getMessage("listing.criteria.riskManagementSummaryInformationNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setRiskManagementSummaryInformation(null);
        }
    }
}
