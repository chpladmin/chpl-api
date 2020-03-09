package old.gov.healthit.chpl.dao.impl;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
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
import gov.healthit.chpl.dao.IncumbentDevelopersStatisticsDAO;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class IncumbentDevelopersStatisticsDAOTest extends TestCase {

    private static final int STAT_LENGTH = 1;
    private static final Long COUNT_2011_TO_2014 = 4L;

    @Autowired
    private IncumbentDevelopersStatisticsDAO idDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void getAllStatistics() {
        List<IncumbentDevelopersStatisticsDTO> results = idDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH, results.size());
        assertEquals(COUNT_2011_TO_2014, results.get(0).getIncumbentCount());
        assertEquals("2014", results.get(0).getOldCertificationEdition().getYear());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void deleteOneStat() throws EntityRetrievalException {
        idDao.delete(-1L);
        List<IncumbentDevelopersStatisticsDTO> results = idDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH - 1, results.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createOneStat() throws EntityCreationException, EntityRetrievalException {
        IncumbentDevelopersStatisticsDTO dto = new IncumbentDevelopersStatisticsDTO();
        dto.setIncumbentCount(2L);
        dto.setNewCount(3L);
        dto.setOldCertificationEditionId(1L);
        dto.setNewCertificationEditionId(2L);
        idDao.create(dto);
        List<IncumbentDevelopersStatisticsDTO> results = idDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH + 1, results.size());
    }
}
