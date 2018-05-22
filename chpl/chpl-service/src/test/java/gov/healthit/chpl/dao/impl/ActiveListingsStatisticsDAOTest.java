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
import gov.healthit.chpl.dao.ActiveListingsStatisticsDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ActiveListingsStatisticsDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ActiveListingsStatisticsDAOTest extends TestCase {

    @Autowired
    private ActiveListingsStatisticsDAO alDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void getAllStatistics() {
        final int expectedCount = 1;
        final Long expectedDeveloperCount = 4L;
        final String expectedEdition = "2014";
        List<ActiveListingsStatisticsDTO> results = alDao.findAll();
        assertNotNull(results);
        assertEquals(expectedCount, results.size());
        assertEquals(expectedDeveloperCount, results.get(0).getDeveloperCount());
        assertEquals(expectedEdition, results.get(0).getCertificationEdition().getYear());
    }

    @Test
    @Transactional
    public void deleteOneStat() throws EntityRetrievalException {
        final int expectedCount = 0;
        alDao.delete(1L);
        List<ActiveListingsStatisticsDTO> results = alDao.findAll();
        assertNotNull(results);
        assertEquals(expectedCount, results.size());
    }

    @Test
    @Transactional
    public void createOneStat() throws EntityCreationException, EntityRetrievalException {
        final int expectedCount = 2;
        ActiveListingsStatisticsDTO dto = new ActiveListingsStatisticsDTO();
        dto.setProductCount(1L);
        dto.setDeveloperCount(1L);
        dto.setCertificationEditionId(2L);
        alDao.create(dto);
        List<ActiveListingsStatisticsDTO> results = alDao.findAll();
        assertNotNull(results);
        assertEquals(expectedCount, results.size());
    }
}
