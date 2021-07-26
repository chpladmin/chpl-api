package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("uploadedListingUnattestedCriteriaWithDataReviewer")
public class UnattestedCriteriaWithDataReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UnattestedCriteriaWithDataReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && listing.getSed().getTestTasks() != null
                && listing.getSed().getTestTasks().size() > 0) {
            listing.getSed().getTestTasks().stream()
                .forEach(testTask -> addWarningIfTestTaskCriterionIsNotAttestedTo(listing, testTask));
        }
        if (listing.getSed() != null && listing.getSed().getUcdProcesses() != null
                && listing.getSed().getUcdProcesses().size() > 0) {
            listing.getSed().getUcdProcesses().stream()
                .forEach(ucdProcess -> addWarningIfUcdProcessCriterionIsNotAttestedTo(listing, ucdProcess));
        }

        listing.getCertificationResults().stream()
            .filter(certResult -> certResult != null && certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                    && BooleanUtils.isFalse(certResult.isSuccess()))
            .forEach(unattestedCertResult -> reviewCertificationResult(listing, unattestedCertResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isGap() != null && certResult.isGap()) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "GAP"));
        }
        if (certResult.isSed() != null && certResult.isSed()) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "SED"));
        }
        if (!StringUtils.isEmpty(certResult.getApiDocumentation())) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "API Documentation"));
        }
        if (!StringUtils.isEmpty(certResult.getServiceBaseUrlList())) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Service Base URL List"));
        }
        if (!StringUtils.isEmpty(certResult.getExportDocumentation())) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Export Documentation"));
        }
        if (!StringUtils.isEmpty(certResult.getDocumentationUrl())) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Documentation URL"));
        }
        if (!StringUtils.isEmpty(certResult.getUseCases())) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Use Cases"));
        }
        if (certResult.getAttestationAnswer() != null && certResult.getAttestationAnswer().booleanValue()) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Attestation Answer"));
        }
        if (!StringUtils.isEmpty(certResult.getPrivacySecurityFramework())) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Privacy and Security Framework"));
        }
        if (certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Additional Software"));
        }
        if (certResult.getTestDataUsed() != null && certResult.getTestDataUsed().size() > 0) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Data"));
        }
        if (certResult.getTestFunctionality() != null && certResult.getTestFunctionality().size() > 0) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Functionality"));
        }
        if (certResult.getTestProcedures() != null && certResult.getTestProcedures().size() > 0) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Procedures"));
        }
        if (certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Standards"));
        }
        if (certResult.getTestToolsUsed() != null && certResult.getTestToolsUsed().size() > 0) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Tools"));
        }
    }

    private void addWarningIfTestTaskCriterionIsNotAttestedTo(CertifiedProductSearchDetails listing, TestTask testTask) {
        testTask.getCriteria().stream()
            .filter(criterion -> !isCriterionAttestedTo(listing, criterion))
            .forEach(criterion -> listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(criterion), "Test Tasks")));
    }

    private void addWarningIfUcdProcessCriterionIsNotAttestedTo(CertifiedProductSearchDetails listing, UcdProcess ucdProcess) {
        ucdProcess.getCriteria().stream()
            .filter(criterion -> !isCriterionAttestedTo(listing, criterion))
            .forEach(criterion -> listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(criterion), "UCD Processes")));
    }

    private boolean isCriterionAttestedTo(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
            .filter(certResult -> certResult.getCriterion().getId().equals(criterion.getId()))
            .findAny().isPresent();
    }
}
