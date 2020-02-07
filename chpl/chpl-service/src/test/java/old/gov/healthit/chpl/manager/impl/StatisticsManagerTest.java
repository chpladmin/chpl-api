package old.gov.healthit.chpl.manager.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

import gov.healthit.chpl.domain.CriterionProductStatistics;
import gov.healthit.chpl.domain.IncumbentDevelopersStatistics;
import gov.healthit.chpl.domain.ListingCountStatistics;
import gov.healthit.chpl.manager.StatisticsManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class StatisticsManagerTest extends TestCase {

    @Autowired
    private StatisticsManager statisticsManager;

    /**
     * Test to ensure the statistics manage can retrieve criterion/product statistics.
     */
    @Test
    @Transactional
    public void testStatisticsManagerCanRetrieveStats() {
        List<CriterionProductStatistics> stats = statisticsManager.getCriterionProductStatisticsResult();
        assertNotNull(stats);

        //Sort so test works consistently
        Collections.sort(stats, new Comparator<CriterionProductStatistics>() {
            @Override
            public int compare(CriterionProductStatistics one, CriterionProductStatistics other) {
                return one.getCertificationCriterionId().compareTo(other.getCertificationCriterionId());
            }
        });

        assertEquals("170.315 (d)(10)", stats.get(0).getCriterion().getNumber());
    }

    /**
     * Test to ensure the statistics manager can retrieve new vs. incumbent developer statistics.
     */
    @Test
    @Transactional
    public void retrieveNewVsIncumbentDeveloperStats() {
        final Long expectedCount = 3L;
        List<IncumbentDevelopersStatistics> stats = statisticsManager.getIncumbentDevelopersStatisticsResult();
        assertNotNull(stats);
        assertEquals(expectedCount, stats.get(0).getNewCount());
    }

    /**
     * Test to ensure the statistics manager can retrieve product/developer statistics.
     */
    @Test
    @Transactional
    public void retrieveListingCountStats() {
        final Long expectedCount = 3L;
        List<ListingCountStatistics> stats = statisticsManager.getListingCountStatisticsResult();
        assertNotNull(stats);
        assertEquals(expectedCount, stats.get(0).getProductCount());
    }
}
