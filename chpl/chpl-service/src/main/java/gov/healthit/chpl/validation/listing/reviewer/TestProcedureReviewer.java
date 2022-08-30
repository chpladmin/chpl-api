package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestProcedureDTO;
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
        checkIfTestProcedureIsAllowedByFlag(listing, certResult);
        if (testProcedure.getTestProcedure().getId() != null) {
            checkIfTestProcedureIsAllowedById(listing, certResult, testProcedure);
        } else if (!StringUtils.isEmpty(testProcedure.getTestProcedure().getName())) {
           checkIfTestProcedureIsAllowedByName(listing, certResult, testProcedure);
        }
        checkIfTestProcedureHasAnId(listing, certResult, testProcedure);
        checkIfTestProcedureHasAName(listing, certResult, testProcedure);
        checkIfTestProcedureHasAVersion(listing, certResult, testProcedure);
    }

    private void checkIfTestProcedureIsAllowedByFlag(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        boolean isAllowed = certResult.getCriterion().getCertificationEdition().equalsIgnoreCase("2014");
            if (!isAllowed) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.testProcedureNotApplicable",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
    }

    private void checkIfTestProcedureIsAllowedById(CertifiedProductSearchDetails listing, CertificationResult certResult,
        CertificationResultTestProcedure testProcedure) {
        Optional<TestProcedureDTO> allowedTestProcedure
            = testProcedureDao.getByCriterionId(certResult.getCriterion().getId()).stream()
                .filter(tp -> tp.getId().equals(testProcedure.getTestProcedure().getId()))
                .findAny();
            if (!allowedTestProcedure.isPresent()) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.badTestProcedureId",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        testProcedure.getTestProcedure().getId());
            } else {
                testProcedure.getTestProcedure().setName(allowedTestProcedure.get().getName());
            }
    }

    private void checkIfTestProcedureIsAllowedByName(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestProcedure testProcedure) {
        Optional<TestProcedureDTO> allowedTestProcedure
            = testProcedureDao.getByCriterionId(certResult.getCriterion().getId()).stream()
                .filter(tp -> tp.getName().equalsIgnoreCase(testProcedure.getTestProcedure().getName()))
                .findAny();
            if (!allowedTestProcedure.isPresent()) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.badTestProcedureName",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        testProcedure.getTestProcedure().getName());
            } else {
                testProcedure.getTestProcedure().setId(allowedTestProcedure.get().getId());
            }
    }

    private void checkIfTestProcedureHasAnId(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestProcedure testProcedure) {
            if (testProcedure.getTestProcedure() == null || testProcedure.getTestProcedure().getId() == null) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.missingTestProcedureId",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
    }

    private void checkIfTestProcedureHasAName(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestProcedure testProcedure) {
            if (testProcedure.getTestProcedure() == null
                    || StringUtils.isEmpty(testProcedure.getTestProcedure().getName())) {
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
