package old.gov.healthit.chpl.dao.surveillance.report;

import java.util.List;

import org.junit.BeforeClass;
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
import gov.healthit.chpl.dao.surveillance.report.QuarterDAO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
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
public class QuarterDaoTest extends TestCase {

    @Autowired
    private QuarterDAO quarterDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional(readOnly = true)
    public void getAllQuarters() {
        List<QuarterDTO> quarters = quarterDao.getAll();
        assertNotNull(quarters);
        assertEquals(4, quarters.size());
        for (QuarterDTO quarter : quarters) {
            assertNotNull(quarter.getId());
            assertNotNull(quarter.getName());
            assertNotNull(quarter.getStartDay());
            assertNotNull(quarter.getStartMonth());
            assertNotNull(quarter.getEndDay());
            assertNotNull(quarter.getEndMonth());
        }
    }

    @Test
    @Transactional(readOnly = true)
    public void getQuarterById() throws EntityRetrievalException {
        Long quarterId = 1L;
        QuarterDTO quarter = quarterDao.getById(quarterId);
        assertNotNull(quarter);
        assertEquals(quarterId.longValue(), quarter.getId().longValue());
    }

    @Test(expected = EntityRetrievalException.class)
    @Transactional(readOnly = true)
    public void getQuarterByIdDoesNotExist() throws EntityRetrievalException {
        quarterDao.getById(-100L);
    }
}
