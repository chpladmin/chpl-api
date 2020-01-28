package gov.healthit.chpl.validation.listing.reviewer;

import java.util.ArrayList;

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
                    cert.setGap(null);
                }
                if (cert.isSed() != null && cert.isSed().booleanValue()) {
                    cert.setSed(null);
                }
                if (!StringUtils.isEmpty(cert.getApiDocumentation())) {
                    cert.setApiDocumentation(null);
                }
                if (!StringUtils.isEmpty(cert.getExportDocumentation())) {
                    cert.setExportDocumentation(null);
                }
                if (!StringUtils.isEmpty(cert.getDocumentationUrl())) {
                    cert.setDocumentationUrl(null);
                }
                if (!StringUtils.isEmpty(cert.getUseCases())) {
                    cert.setUseCases(null);
                }
                if (cert.getAttestationAnswer() != null && cert.getAttestationAnswer().booleanValue()) {
                    cert.setAttestationAnswer(null);
                }
                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    cert.getAdditionalSoftware().clear();
                }
                if (cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    cert.getTestDataUsed().clear();
                }
                if (cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    cert.getTestFunctionality().clear();
                }
                if (cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    cert.getTestProcedures().clear();
                }
                if (cert.getTestStandards() != null && cert.getTestStandards().size() > 0) {
                    cert.getTestStandards().clear();
                }
                if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                    cert.getTestToolsUsed().clear();
                }

                if (listing.getSed() != null && listing.getSed().getTestTasks() != null
                        && listing.getSed().getTestTasks().size() > 0) {
                    for (TestTask tt : listing.getSed().getTestTasks()) {
                        ArrayList<CertificationCriterion> remove = new ArrayList<CertificationCriterion>();
                        for (CertificationCriterion ttCriteria : tt.getCriteria()) {
                            if (ttCriteria.getId() != null && ttCriteria.getId().equals(cert.getCriterion().getId())) {
                                remove.add(ttCriteria);
                            }
                        }
                        tt.getCriteria().removeAll(remove);
                    }
                }
                if (listing.getSed() != null && listing.getSed().getUcdProcesses() != null
                        && listing.getSed().getUcdProcesses().size() > 0) {
                    for (UcdProcess ucd : listing.getSed().getUcdProcesses()) {
                        ArrayList<CertificationCriterion> remove = new ArrayList<CertificationCriterion>();
                        for (CertificationCriterion ucdCriteria : ucd.getCriteria()) {
                            if (ucdCriteria.getId() != null && ucdCriteria.getId().equals(cert.getCriterion().getId())) {
                                remove.add(ucdCriteria);
                            }
                        }
                        ucd.getCriteria().removeAll(remove);
                    }
                }
            }
        }
    }
}
