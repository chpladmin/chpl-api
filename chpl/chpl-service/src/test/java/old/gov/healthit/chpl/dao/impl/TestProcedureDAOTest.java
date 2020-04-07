package old.gov.healthit.chpl.dao.impl;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class TestProcedureDAOTest extends TestCase {

    @Autowired
    private TestProcedureDAO tpDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void findTestProcedureByCriteria() {
        String criteria = "170.314 (a)(1)";
        try {
            List<TestProcedureDTO> testProcedures = tpDao.getByCriterionId(1L);
            assertNotNull(testProcedures);
            assertEquals(1, testProcedures.size());
            assertEquals("ONC Test Method", testProcedures.get(0).getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    @Transactional
    public void findMultipleTestProceduresByCriteria() {
        String criteria = "170.315 (c)(2)";
        try {
            List<TestProcedureDTO> testProcedures = tpDao.getByCriterionId(1L);
            assertNotNull(testProcedures);
            assertEquals(2, testProcedures.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    @Transactional
    public void findTestProcedureByCriteriaAndName() {
        String criteria = "170.314 (a)(1)";
        String tpName = "ONC Test Method";
        try {
            TestProcedureDTO testProcedure = tpDao.getByCriterionIdAndValue(1L, tpName);
            assertNotNull(testProcedure);
            assertEquals(tpName, testProcedure.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    @Transactional
    public void findNoTestProcedureByCriteria() {
        String criteria = "BOGUS";
        List<TestProcedureDTO> testProcedures = tpDao.getByCriterionId(1L);
        assertTrue(testProcedures == null || testProcedures.size() == 0);
    }

    @Test
    @Transactional
    public void findNoTestProcedureByCriteriaAndName() {
        String criteria = "170.314 (a)(1)";
        String tpName = "BOGUS";
        TestProcedureDTO testProcedure = tpDao.getByCriterionIdAndValue(1L, tpName);
        assertNull(testProcedure);
    }

}
