package gov.healthit.chpl.teststandard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TestStandardManager {

    private TestStandardDAO testStandardDao;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDao;

    @Autowired
    public TestStandardManager(TestStandardDAO testStandardDao,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDao) {
        this.testStandardDao = testStandardDao;
        this.certificationCriterionAttributeDao = certificationCriterionAttributeDao;
    }

    @Transactional
    public List<TestStandard> getAll() {
        return testStandardDao.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForTestStandards() {
        return certificationCriterionAttributeDao.getCriteriaForTestStandards();
    }

}
