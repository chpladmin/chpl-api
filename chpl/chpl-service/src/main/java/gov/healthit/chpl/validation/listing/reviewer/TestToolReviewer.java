package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("testToolReviewer")
public class TestToolReviewer extends PermissionBasedReviewer {
    private TestToolDAO testToolDao;

    private List<TestToolCriteriaMap> testToolCriteriaMap;

    @Autowired
    public TestToolReviewer(TestToolDAO testToolDAO, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) throws EntityRetrievalException {
        super(msgUtil, resourcePermissions);
        this.testToolDao = testToolDAO;

        testToolCriteriaMap = testToolDao.getAllTestToolCriteriaMap();
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(cr -> isCertificationResultAttestedTo(cr)
                        && doesCertificationResultHaveTestTools(cr))
                .forEach(cr -> validateTestTools(listing, cr));

    }

    private Boolean isCertificationResultAttestedTo(CertificationResult cert) {
        return cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE);
    }

    private Boolean doesCertificationResultHaveTestTools(CertificationResult cert) {
        return cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0;
    }

    private void validateTestTools(CertifiedProductSearchDetails listing, CertificationResult cert) {
        cert.getTestToolsUsed().stream()
                .forEach(crtt -> validateTestTool(listing, cert, crtt));
    }

    private void validateTestTool(CertifiedProductSearchDetails listing, CertificationResult cert, CertificationResultTestTool testTool) {
        if (StringUtils.isEmpty(testTool.getTestToolName()) && testTool.getTestToolId() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.missingTestToolName",
                    Util.formatCriteriaNumber(cert.getCriterion())));
        } else {
            Optional<TestToolDTO> tt = getTestTool(testTool);
            if (!tt.isPresent()) {
                listing.getErrorMessages().add(msgUtil.getMessage(
                        "listing.criteria.testToolNotFound",
                        Util.formatCriteriaNumber(cert.getCriterion()),
                        testTool.getTestToolName()));
                return;
            }

            if (!isTestToolValidForCriteria(new CertificationCriterionDTO(cert.getCriterion()), tt.get())) {
                listing.getErrorMessages().add(msgUtil.getMessage(
                        "listing.criteria.testToolCriterionMismatch",
                        testTool.getTestToolName(),
                        Util.formatCriteriaNumber(cert.getCriterion())));
                return;
            }

            if (isTestToolRetired(tt.get())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                        "listing.criteria.retiredTestToolNotAllowed",
                        testTool.getTestToolName(),
                        Util.formatCriteriaNumber(cert.getCriterion())));
            }
        }
    }

    private Boolean isTestToolRetired(TestToolDTO testTool) {
        return testTool != null && testTool.isRetired();
    }

    private Boolean isTestToolValidForCriteria(CertificationCriterionDTO criterion, TestToolDTO testTool) {
        return testToolCriteriaMap.stream()
                .filter(ttcm -> ttcm.getCriterion().getId().equals(criterion.getId())
                        && ttcm.getTestTool().getId().equals(testTool.getId()))
                .findAny()
                .isPresent();
    }

    private Optional<TestToolDTO> getTestTool(CertificationResultTestTool certResultTestTool) {
        TestToolDTO testTool = null;
        if (certResultTestTool.getTestToolId() != null) {
            testTool = testToolDao.getById(certResultTestTool.getTestToolId());
        }
        if (testTool == null && !StringUtils.isEmpty(certResultTestTool.getTestToolName())) {
            testTool = testToolDao.getByName(certResultTestTool.getTestToolName());
        }
        return Optional.ofNullable(testTool);
    }


}

