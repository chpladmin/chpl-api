package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.GapAllowedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;

@Component
public class CertificationResultReviewer extends PermissionBasedReviewer {
    private CriteriaReviewer criteriaReviewer;
    private PrivacyAndSecurityFrameworkReviewer privacyAndSecurityFrameworkReviewer;
    private AdditionalSoftwareReviewer additionalSoftwareReviewer;
    private GapAllowedReviewer gapAllowedReviewer;
    private TestToolReviewer testToolReviewer;
    private TestDataReviewer testDataReviewer;
    private TestProcedureReviewer testProcedureReviewer;
    private TestFunctionalityReviewer testFunctionalityReviewer;
    private TestStandardReviewer testStandardReviewer;
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    private OldCriteriaWithoutIcsReviewer oldCriteriaWithoutIcsReviewer;
    private SedG32015Reviewer sedG3Reviewer;
    private CertificationResultRules certResultRules;
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
            @Qualifier("uploadedListingUnattestedCriteriaWithDataReviewer") UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer,
            @Qualifier("oldCriteriaWithoutIcsReviewer") OldCriteriaWithoutIcsReviewer oldCriteriaWithoutIcsReviewer,
            @Qualifier("sedG32015Reviewer") SedG32015Reviewer sedG3Reviewer,
            CertificationResultRules certResultRules, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.criteriaReviewer = criteriaReviewer;
        this.privacyAndSecurityFrameworkReviewer = privacyAndSecurityFrameworkReviewer;
        this.additionalSoftwareReviewer = additionalSoftwareReviewer;
        this.gapAllowedReviewer = gapAllowedReviewer;
        this.testToolReviewer = testToolReviewer;
        this.testDataReviewer = testDataReviewer;
        this.testProcedureReviewer = testProcedureReviewer;
        this.testFunctionalityReviewer = testFunctionalityReviewer;
        this.testStandardReviewer = testStandardReviewer;
        this.unattestedCriteriaWithDataReviewer = unattestedCriteriaWithDataReviewer;
        this.oldCriteriaWithoutIcsReviewer = oldCriteriaWithoutIcsReviewer;
        this.sedG3Reviewer = sedG3Reviewer;
        this.certResultRules = certResultRules;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationResults"));
            return;
        } else if (listing.getCertificationResults().size() == 0 || hasNoAttestedCriteria(listing)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationResults"));
        }

        listing.getCertificationResults().stream()
            .forEach(certResult -> reviewSuccessField(listing, certResult));

        listing.getCertificationResults().stream()
            .filter(certResult -> certResult != null && certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                        && BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> reviewCertResultFields(listing, certResult));
    }

    private boolean hasNoAttestedCriteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult != null && BooleanUtils.isTrue(certResult.isSuccess()))
                .count() == 0;
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

        criteriaReviewer.review(listing);
        privacyAndSecurityFrameworkReviewer.review(listing);
        additionalSoftwareReviewer.review(listing);
        gapAllowedReviewer.review(listing);
        testToolReviewer.review(listing);
        testDataReviewer.review(listing);
        testProcedureReviewer.review(listing);
        testFunctionalityReviewer.review(listing);
        testStandardReviewer.review(listing);
        unattestedCriteriaWithDataReviewer.review(listing);
        oldCriteriaWithoutIcsReviewer.review(listing);
        sedG3Reviewer.review(listing);
    }

    private void reviewGap(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.GAP)
                && certResult.isGap() == null) {
            if (!StringUtils.isEmpty(certResult.getGapStr())) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.invalidGap",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        certResult.getGapStr());
            } else {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.missingGap",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
        }
    }

    private void reviewAdditionalSoftwareString(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!StringUtils.isEmpty(certResult.getHasAdditionalSoftwareStr())
                && certResult.getHasAdditionalSoftware() == null) {
            addCriterionErrorOrWarningByPermission(listing, certResult,
                    "listing.criteria.invalidHasAdditionalSoftware",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    certResult.getHasAdditionalSoftwareStr());
        }
    }

    private void reviewAttestationAnswer(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)
                && certResult.getAttestationAnswer() == null) {
            if (!StringUtils.isEmpty(certResult.getAttestationAnswerStr())) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.invalidAttestationAnswer",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        certResult.getAttestationAnswerStr());
            } else {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.missingAttestationAnswer",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
        }
    }

    private void reviewApiDocumentation(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)
                && StringUtils.isEmpty(certResult.getApiDocumentation())) {
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingApiDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }

    private void reviewExportDocumentation(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                && StringUtils.isEmpty(certResult.getExportDocumentation())) {
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingExportDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }

    private void reviewUseCases(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                && StringUtils.isEmpty(certResult.getUseCases())
                && certResult.getAttestationAnswer() != null && certResult.getAttestationAnswer().equals(Boolean.TRUE)) {
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingUseCases",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        } else if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                && !StringUtils.isEmpty(certResult.getUseCases())
                && (certResult.getAttestationAnswer() == null || certResult.getAttestationAnswer().equals(Boolean.FALSE))) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                            Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewServiceBaseUrlList(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                && StringUtils.isEmpty(certResult.getServiceBaseUrlList())) {
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingServiceBaseUrlList",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }
}
