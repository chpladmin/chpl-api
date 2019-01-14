package gov.healthit.chpl.dao.impl;

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
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
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
public class TestFunctionalityDAOTest extends TestCase {

    @Autowired
    private TestFunctionalityDAO tfDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void findTestFunctionalityByNumberAndEdition() {
        String number = "(a)(4)(iii)";
        Long editionId = 2L;
        TestFunctionalityDTO foundTf = tfDao.getByNumberAndEdition(number, editionId);
        assertNotNull(foundTf);
        assertEquals(number, foundTf.getNumber());
        assertEquals("2014", foundTf.getYear());
    }

    @Test
    @Transactional
    public void findNoTestFunctionalityByNumberAndEdition() {
        String number = "BOGUS";
        Long editionId = 2L;
        TestFunctionalityDTO foundTf = tfDao.getByNumberAndEdition(number, editionId);
        assertNull(foundTf);
    }

}
