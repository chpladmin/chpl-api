package gov.healthit.chpl.upload.listing.validation.reviewer;

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

@Component
public class CertificationResultReviewer extends PermissionBasedReviewer {
    private PrivacyAndSecurityFrameworkReviewer privacyAndSecurityFrameworkReviewer;
    private TestToolReviewer testToolReviewer;
    private TestDataReviewer testDataReviewer;
    private TestProcedureReviewer testProcedureReviewer;
    private TestFunctionalityReviewer testFunctionalityReviewer;
    private TestStandardReviewer testStandardReviewer;
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public CertificationResultReviewer(@Qualifier("listingUploadPrivacyAndSecurityFrameworkReviewer") PrivacyAndSecurityFrameworkReviewer privacyAndSecurityFrameworkReviewer,
            @Qualifier("listingUploadTestToolReviewer") TestToolReviewer testToolReviewer,
            @Qualifier("listingUploadTestDataReviewer") TestDataReviewer testDataReviewer,
            @Qualifier("listingUploadTestProcedureReviewer") TestProcedureReviewer testProcedureReviewer,
            @Qualifier("listingUploadTestFunctionalityReviewer") TestFunctionalityReviewer testFunctionalityReviewer,
            @Qualifier("listingUploadTestStandardReviewer") TestStandardReviewer testStandardReviewer,
            @Qualifier("uploadedListingUnattestedCriteriaWithDataReviewer") UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer,
            CertificationResultRules certResultRules, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.privacyAndSecurityFrameworkReviewer = privacyAndSecurityFrameworkReviewer;
        this.testToolReviewer = testToolReviewer;
        this.testDataReviewer = testDataReviewer;
        this.testProcedureReviewer = testProcedureReviewer;
        this.testFunctionalityReviewer = testFunctionalityReviewer;
        this.testStandardReviewer = testStandardReviewer;
        this.unattestedCriteriaWithDataReviewer = unattestedCriteriaWithDataReviewer;
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
            .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess())
            .forEach(certResult -> reviewCertResultFields(listing, certResult));
        privacyAndSecurityFrameworkReviewer.review(listing);
        //TODO: additional software reviewer
        //TODO: GAP reviewer? (f3 is not allowed to have GAP = true if a listing certification date is after cures effective date)
        testToolReviewer.review(listing);
        testDataReviewer.review(listing);
        testProcedureReviewer.review(listing);
        testFunctionalityReviewer.review(listing);
        testStandardReviewer.review(listing);
        unattestedCriteriaWithDataReviewer.review(listing);
    }

    private boolean hasNoAttestedCriteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess())
                .count() == 0;
    }

    //TODO: add unit tests
    private void reviewCertResultFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)
                && certResult.getAttestationAnswer() == null) {
            addCriterionErrorOrWarningByPermission(listing, certResult,
                    "listing.criteria.missingAttestationAnswer",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }

        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)
                && StringUtils.isEmpty(certResult.getApiDocumentation())) {
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingApiDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                && StringUtils.isEmpty(certResult.getExportDocumentation())) {
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingExportDocumentation",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }

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

        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                && StringUtils.isEmpty(certResult.getServiceBaseUrlList())) {
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingServiceBaseUrlList",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }
}
