package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("testToolReviewer")
public class TestToolReviewer extends PermissionBasedReviewer {
    private TestToolDAO testToolDao;

        @Autowired
    public TestToolReviewer(TestToolDAO testToolDAO, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) throws EntityRetrievalException {
        super(msgUtil, resourcePermissions);
        this.testToolDao = testToolDAO;
    }

    @Transactional
    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(cr -> isCertificationResultAttestedTo(cr)
                        && BooleanUtils.isFalse(cr.getCriterion().getRemoved())
                        && doesCertificationResultHaveTestTools(cr))
                .forEach(cr -> validateTestTools(listing, cr));
    }

    private Boolean doesCertificationResultHaveTestTools(CertificationResult cert) {
        return cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0;
    }

    private void validateTestTools(CertifiedProductSearchDetails listing, CertificationResult cert) {
        cert.getTestToolsUsed().stream()
                .forEach(crtt -> validateTestTool(listing, cert, crtt));
    }

    private void validateTestTool(CertifiedProductSearchDetails listing, CertificationResult cert, CertificationResultTestTool testTool) {
        if (StringUtils.isEmpty(testTool.getTestTool().getValue()) && testTool.getTestTool().getId() == null) {
            listing.addDataErrorMessage(msgUtil.getMessage(
                    "listing.criteria.missingTestToolName",
                    Util.formatCriteriaNumber(cert.getCriterion())));
        } else {
            Optional<TestTool> tt = getTestTool(testTool);
            if (!tt.isPresent()) {
                listing.addDataErrorMessage(msgUtil.getMessage(
                        "listing.criteria.testToolNotFound",
                        Util.formatCriteriaNumber(cert.getCriterion()),
                        testTool.getTestTool().getValue()));
                return;
            }

            if (!isTestToolValidForCriteria(cert.getCriterion(), tt.get())) {
                listing.addBusinessErrorMessage(msgUtil.getMessage(
                        "listing.criteria.testToolCriterionMismatch",
                        testTool.getTestTool().getValue(),
                        Util.formatCriteriaNumber(cert.getCriterion())));
                return;
            }

            if (isTestToolRetired(tt.get())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.retiredTestToolNotAllowed",
                        testTool.getTestTool().getValue(),
                        Util.formatCriteriaNumber(cert.getCriterion())));
            }
        }
    }

    private Boolean isTestToolRetired(TestTool testTool) {
        return testTool != null && testTool.isRetired();
    }

    private Boolean isTestToolValidForCriteria(CertificationCriterion criterion, TestTool testTool) {
        try {
            return testToolDao.getAllTestToolCriteriaMap().stream()
                .filter(ttcm -> ttcm.getCriterion().getId().equals(criterion.getId())
                        && ttcm.getTestTool().getId().equals(testTool.getId()))
                .findAny()
                .isPresent();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not validate Test Tool for {}", criterion.getNumber(), e);
            return false;
        }
    }

    private Optional<TestTool> getTestTool(CertificationResultTestTool certResultTestTool) {
        TestTool testTool = null;
        if (certResultTestTool.getTestTool().getId() != null) {
            testTool = testToolDao.getById(certResultTestTool.getTestTool().getId());
        } else if (!StringUtils.isEmpty(certResultTestTool.getTestTool().getValue())) {
            testTool = testToolDao.getByName(certResultTestTool.getTestTool().getValue());
        }
        return Optional.ofNullable(testTool);
    }
}
