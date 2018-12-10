package gov.healthit.chpl.dao.impl;

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
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dto.TestDataDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class TestDataDAOTest extends TestCase {

    @Autowired
    private TestDataDAO tdDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void findTestDataByCriteria() {
        String criteria = "170.314 (a)(1)";
        try {
            List<TestDataDTO> testData = tdDao.getByCriteriaNumber(criteria);
            assertNotNull(testData);
            assertEquals(1, testData.size());
            assertEquals("ONC Test Method", testData.get(0).getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    @Transactional
    public void findMultipleTestDataByCriteria() {
        String criteria = "170.315 (c)(2)";
        try {
            List<TestDataDTO> testData = tdDao.getByCriteriaNumber(criteria);
            assertNotNull(testData);
            assertEquals(2, testData.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    @Transactional
    public void findTestDataByCriteriaAndName() {
        String criteria = "170.314 (a)(1)";
        String tdName = "ONC Test Method";
        try {
            TestDataDTO testData = tdDao.getByCriteriaNumberAndValue(criteria, tdName);
            assertNotNull(testData);
            assertEquals(tdName, testData.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    @Transactional
    public void findNoTestDataByCriteria() {
        String criteria = "BOGUS";
        List<TestDataDTO> testData = tdDao.getByCriteriaNumber(criteria);
        assertTrue(testData == null || testData.size() == 0);
    }

    @Test
    @Transactional
    public void findNoTestDataByCriteriaAndName() {
        String criteria = "170.314 (a)(1)";
        String tdName = "BOGUS";
        TestDataDTO testData = tdDao.getByCriteriaNumberAndValue(criteria, tdName);
        assertNull(testData);
    }

}
