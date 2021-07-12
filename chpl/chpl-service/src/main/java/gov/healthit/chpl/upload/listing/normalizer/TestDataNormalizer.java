package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestData;
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
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateTestDataIds(certResult.getCriterion(), certResult.getTestDataUsed()));
        }
    }

    private void populateTestDataIds(CertificationCriterion criterion,
            List<CertificationResultTestData> testDatas) {
        if (testDatas != null && testDatas.size() > 0) {
            testDatas.stream()
                .filter(testData -> isMissingName(testData))
                .forEach(testData -> fillInDefaultName(testData));
            testDatas.stream()
                .forEach(testData -> populateTestDataId(criterion, testData));
        }
    }

    private boolean isMissingName(CertificationResultTestData testData) {
        if (testData == null) {
            return false;
        }
        return (!StringUtils.isEmpty(testData.getVersion()) || !StringUtils.isEmpty(testData.getAlteration()))
                && (testData.getTestData() == null || StringUtils.isEmpty(testData.getTestData().getName()));
    }

    private void fillInDefaultName(CertificationResultTestData testData) {
        if (testData != null) {
            if (testData.getTestData() == null) {
                testData.setTestData(new TestData());
            }
            testData.getTestData().setName(TestDataDTO.DEFALUT_TEST_DATA);
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

            if (testData.getTestData().getId() == null) {
                replaceInvalidTestDataWithDefault(criterion, testData);
            }
        }
    }

    private void replaceInvalidTestDataWithDefault(CertificationCriterion criterion, CertificationResultTestData testData) {
        TestDataDTO defaultTestData = testDataDao.getByCriterionAndValue(criterion.getId(), TestDataDTO.DEFALUT_TEST_DATA);
        if (defaultTestData != null) {
            testData.getTestData().setId(defaultTestData.getId());
            testData.getTestData().setName(defaultTestData.getName());
        }
    }
}
