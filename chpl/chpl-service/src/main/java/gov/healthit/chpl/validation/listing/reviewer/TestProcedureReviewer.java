package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("testProcedureReviewer")
public class TestProcedureReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private TestProcedureDAO testProcedureDao;

    @Autowired
    public TestProcedureReviewer(TestProcedureDAO testProcedureDao, ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        this.testProcedureDao = testProcedureDao;
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

        boolean isAllowed = testProcedureDao.getByCriterionId(certResult.getCriterion().getId()).stream()
                .anyMatch(tp -> tp.getId() == testProcedure.getTestProcedure().getId());
            if (!isAllowed) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.badTestProcedureName",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        testProcedure.getTestProcedure().getName()));
            }
    }
}
