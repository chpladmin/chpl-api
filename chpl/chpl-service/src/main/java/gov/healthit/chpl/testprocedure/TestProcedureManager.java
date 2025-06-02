package gov.healthit.chpl.testprocedure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TestProcedureManager {

    private TestProcedureDAO testProcedureDao;

    @Autowired
    public TestProcedureManager(TestProcedureDAO testProcedureDao) {
        this.testProcedureDao = testProcedureDao;
    }

    @Transactional
    public List<TestProcedure> getAll() {
        return testProcedureDao.getAll();
    }

    @Transactional
    public List<TestProcedureCriteriaMap> getAllWithMappedCriteria() {
        return testProcedureDao.findAllWithMappedCriteria();
    }
}
