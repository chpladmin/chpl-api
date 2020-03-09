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
import gov.healthit.chpl.dao.ListingCountStatisticsDAO;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
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
public class ListingCountStatisticsDAOTest extends TestCase {

    @Autowired
    private ListingCountStatisticsDAO lcDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void getAllStatistics() {
        final int expectedCount = 1;
        final Long expectedDeveloperCount = 4L;
        final String expectedEdition = "2014";
        final String expectedStatus = "Active";
        List<ListingCountStatisticsDTO> results = lcDao.findAll();
        assertNotNull(results);
        assertEquals(expectedCount, results.size());
        assertEquals(expectedDeveloperCount, results.get(0).getDeveloperCount());
        assertEquals(expectedEdition, results.get(0).getCertificationEdition().getYear());
        assertEquals(expectedStatus, results.get(0).getCertificationStatus().getStatus());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void deleteOneStat() throws EntityRetrievalException {
        final int expectedCount = 0;
        lcDao.delete(-1L);
        List<ListingCountStatisticsDTO> results = lcDao.findAll();
        assertNotNull(results);
        assertEquals(expectedCount, results.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createOneStat() throws EntityCreationException, EntityRetrievalException {
        final int expectedCount = 2;
        ListingCountStatisticsDTO dto = new ListingCountStatisticsDTO();
        dto.setProductCount(1L);
        dto.setDeveloperCount(1L);
        dto.setCertificationEditionId(2L);
        dto.setCertificationStatusId(1L);
        lcDao.create(dto);
        List<ListingCountStatisticsDTO> results = lcDao.findAll();
        assertNotNull(results);
        assertEquals(expectedCount, results.size());
    }
}
