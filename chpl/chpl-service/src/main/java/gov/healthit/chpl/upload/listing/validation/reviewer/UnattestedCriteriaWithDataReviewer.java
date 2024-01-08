package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.TestTask;
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
            .filter(certResult -> certResult != null && certResult.getCriterion() != null
                    && certResult.getCriterion().getId() != null
                    && BooleanUtils.isFalse(certResult.getCriterion().isRemoved())
                    && BooleanUtils.isNotTrue(certResult.getSuccess()))
            .forEach(unattestedCertResult -> reviewCertificationResult(listing, unattestedCertResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getGap() != null && certResult.getGap()) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "GAP"));
        }
        if (certResult.getSed() != null && certResult.getSed()) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "SED"));
        }
        if (!StringUtils.isEmpty(certResult.getApiDocumentation())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "API Documentation"));
        }
        if (!StringUtils.isEmpty(certResult.getServiceBaseUrlList())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Service Base URL List"));
        }
        if (!StringUtils.isEmpty(certResult.getRiskManagementSummaryInformation())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Risk Management Summary Information"));
        }
        if (!StringUtils.isEmpty(certResult.getExportDocumentation())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Export Documentation"));
        }
        if (!StringUtils.isEmpty(certResult.getDocumentationUrl())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Documentation URL"));
        }
        if (!StringUtils.isEmpty(certResult.getUseCases())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Use Cases"));
        }
        if (certResult.getAttestationAnswer() != null && certResult.getAttestationAnswer().booleanValue()) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Attestation Answer"));
        }
        if (!StringUtils.isEmpty(certResult.getPrivacySecurityFramework())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Privacy and Security Framework"));
        }
        if (!CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Additional Software"));
        }
        if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Data"));
        }
        if (!CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Functionality Tested"));
        }
        if (!CollectionUtils.isEmpty(certResult.getTestProcedures())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Procedures"));
        }
        if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Conformance Methods"));
        }
        if (!CollectionUtils.isEmpty(certResult.getTestStandards())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Standards"));
        }
        if (!CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Optional Standards"));
        }
        if (!CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Test Tools"));
        }
        if (!CollectionUtils.isEmpty(certResult.getSvaps())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(certResult.getCriterion()), "Standards Version Advancement Processes"));
        }
    }

    private void addWarningIfTestTaskCriterionIsNotAttestedTo(CertifiedProductSearchDetails listing, TestTask testTask) {
        testTask.getCriteria().stream()
            .filter(criterion -> !isCriterionAttestedTo(listing, criterion))
            .forEach(criterion -> listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(criterion), "Test Tasks")));
    }

    private void addWarningIfUcdProcessCriterionIsNotAttestedTo(CertifiedProductSearchDetails listing, CertifiedProductUcdProcess ucdProcess) {
        ucdProcess.getCriteria().stream()
            .filter(criterion -> !isCriterionAttestedTo(listing, criterion))
            .forEach(criterion -> listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            Util.formatCriteriaNumber(criterion), "UCD Processes")));
    }

    private boolean isCriterionAttestedTo(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.getSuccess()))
            .filter(certResult -> certResult.getCriterion().getId().equals(criterion.getId()))
            .findAny().isPresent();
    }
}
