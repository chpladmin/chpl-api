package old.gov.healthit.chpl.dao.impl;

import java.util.Collections;
import java.util.Comparator;
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
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
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
public class CriterionProductStatisticsDAOTest extends TestCase {

    private static final int STAT_LENGTH = 2;

    @Autowired
    private CriterionProductStatisticsDAO cpsDao;

    @Autowired
    private CertificationCriterionDAO ccDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void getAllStatistics() {
        List<CriterionProductStatisticsDTO> results = cpsDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH, results.size());

        //Sort so test works consistently
        Collections.sort(results, new Comparator<CriterionProductStatisticsDTO>() {
            @Override
            public int compare(CriterionProductStatisticsDTO one, CriterionProductStatisticsDTO other) {
                return one.getCertificationCriterionId().compareTo(other.getCertificationCriterionId());
            }
        });
        assertEquals("170.315 (d)(10)", results.get(0).getCriteria().getNumber());
        assertEquals("2015", results.get(0).getCriteria().getCertificationEdition());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void deleteOneStat() throws EntityRetrievalException {
        cpsDao.delete(-1L);
        List<CriterionProductStatisticsDTO> results = cpsDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH - 1, results.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createOneStat() throws EntityCreationException, EntityRetrievalException {
        CriterionProductStatisticsDTO dto = new CriterionProductStatisticsDTO();
        dto.setCertificationCriterionId(2L);
        dto.setProductCount(1L);
        dto.setCriteria(ccDao.getById(2L));
        cpsDao.create(dto);
        List<CriterionProductStatisticsDTO> results = cpsDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH + 1, results.size());
    }
}
