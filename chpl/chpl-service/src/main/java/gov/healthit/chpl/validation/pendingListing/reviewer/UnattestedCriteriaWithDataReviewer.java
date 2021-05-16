package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingUnattestedCriteriaWithDataReviewer")
public class UnattestedCriteriaWithDataReviewer implements Reviewer {

    @Autowired ErrorMessageUtil msgUtil;

    public void review(PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if ((cert.getMeetsCriteria() == null || !cert.getMeetsCriteria().booleanValue())) {
                if (cert.getGap() != null && cert.getGap().booleanValue()) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "GAP"));
                }
                if (cert.getSed() != null && cert.getSed().booleanValue()) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "SED"));
                }
                if (!StringUtils.isEmpty(cert.getApiDocumentation())) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "API Documentation"));
                }
                if (!StringUtils.isEmpty(cert.getServiceBaseUrlList())) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Service Base URL List"));
                }
                if (!StringUtils.isEmpty(cert.getExportDocumentation())) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Export Documentation"));
                }
                if (!StringUtils.isEmpty(cert.getDocumentationUrl())) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Documentation URL"));
                }
                if (!StringUtils.isEmpty(cert.getUseCases())) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Use Cases"));
                }
                if (cert.getAttestationAnswer() != null && cert.getAttestationAnswer().booleanValue()) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Attestation Answer"));
                }
                if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Privacy and Security Framework"));
                }
                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Additional Software"));
                }
                if (cert.getTestData() != null && cert.getTestData().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Test Data"));
                }
                if (cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Test Functionality"));
                }
                if (cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Test Procedures"));
                }
                if (cert.getTestStandards() != null && cert.getTestStandards().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Test Standards"));
                }
                if (cert.getTestTasks() != null && cert.getTestTasks().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Test Tasks"));
                }
                if (cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "Test Tools"));
                }
                if (cert.getUcdProcesses() != null && cert.getUcdProcesses().size() > 0) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                                    Util.formatCriteriaNumber(cert.getCriterion()), "UCD Processes"));
                }
            }
        }
    }
}
