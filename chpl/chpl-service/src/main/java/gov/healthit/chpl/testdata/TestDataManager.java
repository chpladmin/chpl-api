package gov.healthit.chpl.testdata;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TestDataManager {

    private TestDataDAO testDataDao;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDao;

    @Autowired
    public TestDataManager(TestDataDAO testDataDao,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDao) {
        this.testDataDao = testDataDao;
        this.certificationCriterionAttributeDao = certificationCriterionAttributeDao;
    }

    @Transactional
    public List<TestData> getAll() {
        return testDataDao.getAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForTestData() {
        return certificationCriterionAttributeDao.getCriteriaForTestData();
    }

}
