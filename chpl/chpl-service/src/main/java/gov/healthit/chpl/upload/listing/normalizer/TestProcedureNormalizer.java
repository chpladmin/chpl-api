package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.dto.TestProcedureDTO;

@Component
public class TestProcedureNormalizer {
    private FF4j ff4j;
    private TestProcedureDAO testProcedureDao;

    @Autowired
    public TestProcedureNormalizer(TestProcedureDAO testProcedureDao, FF4j ff4j) {
        this.testProcedureDao = testProcedureDao;
        this.ff4j = ff4j;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            return;
        }

        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateTestProcedureIds(certResult.getCriterion(), certResult.getTestProcedures()));
        }
    }

    private void populateTestProcedureIds(CertificationCriterion criterion,
            List<CertificationResultTestProcedure> testProcedures) {
        if (testProcedures != null && testProcedures.size() > 0) {
            testProcedures.stream()
                .filter(testProcedure -> isMissingName(testProcedure))
                .forEach(testProcedure -> fillInDefaultName(testProcedure));
            testProcedures.stream()
                .forEach(testProcedure -> populateTestProcedureId(criterion, testProcedure));
        }
    }

    private boolean isMissingName(CertificationResultTestProcedure testProcedure) {
        if (testProcedure == null) {
            return false;
        }
        return !StringUtils.isEmpty(testProcedure.getTestProcedureVersion())
                && (testProcedure.getTestProcedure() == null || StringUtils.isEmpty(testProcedure.getTestProcedure().getName()));
    }

    private void fillInDefaultName(CertificationResultTestProcedure testProcedure) {
        if (testProcedure != null) {
            if (testProcedure.getTestProcedure() == null) {
                testProcedure.setTestProcedure(new TestProcedure());
            }
            testProcedure.getTestProcedure().setName(TestProcedureDTO.DEFAULT_TEST_PROCEDURE);
        }
    }

    private void populateTestProcedureId(CertificationCriterion criterion, CertificationResultTestProcedure testProcedure) {
        if (testProcedure != null && testProcedure.getTestProcedure() != null
                && !StringUtils.isEmpty(testProcedure.getTestProcedure().getName())) {

            List<TestProcedureDTO> allowedTestProcedures = testProcedureDao.getByCriterionId(criterion.getId());
            if (allowedTestProcedures != null && allowedTestProcedures.size() > 0) {
                for (TestProcedureDTO allowedTp : allowedTestProcedures) {
                    if (allowedTp.getName().equalsIgnoreCase(testProcedure.getTestProcedure().getName())) {
                        testProcedure.getTestProcedure().setId(allowedTp.getId());
                    }
                }
            }
        }
    }
}
