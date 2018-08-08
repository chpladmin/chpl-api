package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("unattestedCriteriaWithDataReviewer")
public class UnattestedCriteriaWithDataReviewer implements Reviewer {
    
    @Autowired ErrorMessageUtil msgUtil;
    
    public void review(CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if ((cert.isSuccess() == null || !cert.isSuccess().booleanValue())) {
                if (cert.isGap() != null && cert.isGap().booleanValue()) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData", cert.getNumber(), "GAP"));
                }
                if (cert.isSed() != null && cert.isSed().booleanValue()) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData", cert.getNumber(), "SED"));
                }
                if (!StringUtils.isEmpty(cert.getApiDocumentation())) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData", 
                            cert.getNumber(), "API Documentation"));
                }
                if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData", cert.getNumber(), "API Documentation"));
                }
                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData", 
                            cert.getNumber(), "Additional Software"));
                }
                if (cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            cert.getNumber(), "Test Data"));
                }
                if (cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            cert.getNumber(), "Test Functionality"));
                }
                if (cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            cert.getNumber(), "Test Procedures"));
                }
                if (cert.getTestStandards() != null && cert.getTestStandards().size() > 0) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            cert.getNumber(), "Test Standards"));
                }
                if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                    listing.getWarningMessages()
                    .add(msgUtil.getMessage("listing.criteria.falseCriteriaHasData",
                            cert.getNumber(), "Test Tools"));
                }

                if (listing.getSed() != null && listing.getSed().getTestTasks() != null
                        && listing.getSed().getTestTasks().size() > 0) {
                    for (TestTask tt : listing.getSed().getTestTasks()) {
                        for (CertificationCriterion ttCriteria : tt.getCriteria()) {
                            if (ttCriteria.getNumber() != null && ttCriteria.getNumber().equals(cert.getNumber())) {
                                listing.getWarningMessages().add(msgUtil.getMessage(
                                        "listing.criteria.falseCriteriaHasData",
                                        cert.getNumber(), "Test Tasks"));
                            }
                        }
                    }
                }
                if (listing.getSed() != null && listing.getSed().getUcdProcesses() != null
                        && listing.getSed().getUcdProcesses().size() > 0) {
                    for (UcdProcess ucd : listing.getSed().getUcdProcesses()) {
                        for (CertificationCriterion ucdCriteria : ucd.getCriteria()) {
                            if (ucdCriteria.getNumber() != null && ucdCriteria.getNumber().equals(cert.getNumber())) {
                                listing.getWarningMessages().add(msgUtil.getMessage(
                                        "listing.criteria.falseCriteriaHasData",
                                        cert.getNumber(), "UCD Processes"));
                            }
                        }
                    }
                }
            }
        }
    }
}
