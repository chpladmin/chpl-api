package gov.healthit.chpl.testprocedure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TestProcedureManager {

    private TestProcedureDAO testProcedureDao;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDao;

    @Autowired
    public TestProcedureManager(TestProcedureDAO testProcedureDao,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDao) {
        this.testProcedureDao = testProcedureDao;
        this.certificationCriterionAttributeDao = certificationCriterionAttributeDao;
    }

    @Transactional
    public List<TestProcedure> getAll() {
        return testProcedureDao.getAll();
    }

    @Transactional
    public List<TestProcedureCriteriaMap> getAllWithMappedCriteria() {
        return testProcedureDao.findAllWithMappedCriteria();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForTestProcedures() {
        return certificationCriterionAttributeDao.getCriteriaForTestProcedures();
    }
}
