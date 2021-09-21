package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
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
    private FF4j ff4j;

    @Autowired
    public TestProcedureReviewer(TestProcedureDAO testProcedureDao, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions, FF4j ff4j) {
        super(msgUtil, resourcePermissions);
        this.msgUtil = msgUtil;
        this.testProcedureDao = testProcedureDao;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
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
        checkIfTestProcedureIsAllowedByFlag(listing, certResult);
        checkIfTestProcedureIsAllowed(listing, certResult, testProcedure);
        checkIfTestProcedureHasAName(listing, certResult, testProcedure);
        checkIfTestProcedureHasAVersion(listing, certResult, testProcedure);
    }

    private void checkIfTestProcedureIsAllowedByFlag(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            return;
        }
        boolean isAllowed = certResult.getCriterion().getCertificationEdition().equalsIgnoreCase("2014");
            if (!isAllowed) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.testProcedureNotApplicable",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
    }

    private void checkIfTestProcedureIsAllowed(CertifiedProductSearchDetails listing, CertificationResult certResult,
        CertificationResultTestProcedure testProcedure) {
        boolean isAllowed = testProcedureDao.getByCriterionId(certResult.getCriterion().getId()).stream()
                .anyMatch(tp -> tp.getId() == testProcedure.getTestProcedure().getId());
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
