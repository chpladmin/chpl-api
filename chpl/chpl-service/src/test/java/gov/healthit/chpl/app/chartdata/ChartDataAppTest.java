package gov.healthit.chpl.app.chartdata;

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
import gov.healthit.chpl.dao.IncumbentDevelopersStatisticsDAO;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ChartDataAppTest extends TestCase {

    private static final int STAT_LENGTH = 1;
    private static final Long COUNT_2011_TO_2014 = 4L;

    @Autowired
    private IncumbentDevelopersStatisticsDAO idDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void buildIncumbentStatistics() {
        List<IncumbentDevelopersStatisticsDTO> results = idDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH, results.size());
        assertEquals(COUNT_2011_TO_2014, results.get(0).getIncumbentCount());
        assertEquals("2014", results.get(0).getOldCertificationEdition().getYear());

        ChartData.main(null);

        results = idDao.findAll();
        assertNotNull(results);
        assertEquals(STAT_LENGTH, results.size());
        assertEquals(COUNT_2011_TO_2014, results.get(0).getIncumbentCount());
        assertEquals("2014", results.get(0).getOldCertificationEdition().getYear());
    }
}
