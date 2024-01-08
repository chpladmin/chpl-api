package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestDataDTO;

@Component
public class TestDataNormalizer {
    private TestDataDAO testDataDao;

    @Autowired
    public TestDataNormalizer(TestDataDAO testDataDao) {
        this.testDataDao = testDataDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            clearDataForUnattestedCriteria(listing);
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateTestDataIds(certResult.getCriterion(), certResult.getTestDataUsed()));
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.getSuccess() == null || BooleanUtils.isFalse(certResult.getSuccess()))
                    && certResult.getTestDataUsed() != null && certResult.getTestDataUsed().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getTestDataUsed().clear());
    }

    private void populateTestDataIds(CertificationCriterion criterion,
            List<CertificationResultTestData> testDatas) {
        if (testDatas != null && testDatas.size() > 0) {
            testDatas.stream()
                .forEach(testData -> populateTestDataId(criterion, testData));
        }
    }

    private void populateTestDataId(CertificationCriterion criterion, CertificationResultTestData testData) {
        if (testData != null && testData.getTestData() != null
                && !StringUtils.isEmpty(testData.getTestData().getName())) {

            List<TestDataDTO> allowedTestData = testDataDao.getByCriterionId(criterion.getId());
            if (allowedTestData != null && allowedTestData.size() > 0) {
                for (TestDataDTO allowedTd : allowedTestData) {
                    if (allowedTd.getName().equalsIgnoreCase(testData.getTestData().getName())) {
                        testData.getTestData().setId(allowedTd.getId());
                    }
                }
            }
        }
    }
}
