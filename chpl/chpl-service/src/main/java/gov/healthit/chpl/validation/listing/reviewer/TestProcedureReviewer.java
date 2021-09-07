package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("testProcedureReviewer")
public class TestProcedureReviewer extends PermissionBasedReviewer {
    private TestProcedureDAO testProcedureDao;

    @Autowired
    public TestProcedureReviewer(TestProcedureDAO testProcedureDao, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.msgUtil = msgUtil;
        this.testProcedureDao = testProcedureDao;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(cert -> (cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0))
            .forEach(certResult -> certResult.getTestProcedures().stream()
                    .forEach(testProcedure -> reviewTestProcedure(listing, certResult, testProcedure)));
    }

    private void reviewTestProcedure(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestProcedure testProcedure) {
        checkIfTestProcedureIsAllowed(listing, certResult, testProcedure);
        checkIfTestProcedureHasAName(listing, certResult, testProcedure);
        checkIfTestProcedureHasAVersion(listing, certResult, testProcedure);
    }

    private void checkIfTestProcedureIsAllowed(CertifiedProductSearchDetails listing, CertificationResult certResult,
        CertificationResultTestProcedure testProcedure) {
        boolean isAllowed = testProcedureDao.getByCriterionId(certResult.getCriterion().getId()).stream()
                .anyMatch(tp -> tp.getId().equals(testProcedure.getTestProcedure().getId()));
            if (!isAllowed) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.badTestProcedureName",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        testProcedure.getTestProcedure().getName());
            }
    }

    private void checkIfTestProcedureHasAName(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestProcedure testProcedure) {
                if (testProcedure.getTestProcedure() == null) {
                    addCriterionErrorOrWarningByPermission(listing, certResult,
                            "listing.criteria.missingTestProcedureName",
                            Util.formatCriteriaNumber(certResult.getCriterion()));
                }
        }

    private void checkIfTestProcedureHasAVersion(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestProcedure testProcedure) {
                if (testProcedure.getTestProcedure() != null
                        && !StringUtils.isEmpty(testProcedure.getTestProcedure().getName())
                        && StringUtils.isEmpty(testProcedure.getTestProcedureVersion())) {
                    addCriterionErrorOrWarningByPermission(listing, certResult,
                            "listing.criteria.missingTestProcedureVersion",
                            Util.formatCriteriaNumber(certResult.getCriterion()));
                }
        }
}
