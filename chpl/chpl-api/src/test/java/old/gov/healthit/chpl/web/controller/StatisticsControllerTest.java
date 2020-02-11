package old.gov.healthit.chpl.web.controller;

import java.util.Collections;
import java.util.Comparator;

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
import gov.healthit.chpl.domain.CriterionProductStatistics;
import gov.healthit.chpl.web.controller.StatisticsController;
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import gov.healthit.chpl.web.controller.results.IncumbentDevelopersStatisticsResult;
import gov.healthit.chpl.web.controller.results.ListingCountStatisticsResult;
import junit.framework.TestCase;

/**
 * Tests for Statistics controller.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class StatisticsControllerTest extends TestCase {
    @Autowired
    private StatisticsController statisticsController;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    /**
     * Get statistics for criterion-product data.
     */
    @Transactional
    @Test
    public void testCriterionProductCall() {
        final int expectedCount = 2;
        CriterionProductStatisticsResult resp = statisticsController.getCriterionProductStatistics();

        assertNotNull(resp);
        assertNotNull(resp.getCriterionProductStatisticsResult());

        //Sort so test works consistently
        Collections.sort(resp.getCriterionProductStatisticsResult(), new Comparator<CriterionProductStatistics>() {
            @Override
            public int compare(CriterionProductStatistics one, CriterionProductStatistics other) {
                return one.getCertificationCriterionId().compareTo(other.getCertificationCriterionId());
            }
        });
        assertEquals(expectedCount, resp.getCriterionProductStatisticsResult().size());
        assertEquals("170.315 (d)(10)", resp.getCriterionProductStatisticsResult().get(0).getCriterion().getNumber());
    }

    /**
     * Get statistics for incumbent developers data.
     */
    @Test
    @Transactional
    public void retrieveNewVsIncumbentDeveloperStats() {
        final int expectedSize = 1;
        final Long expectedCount = 4L;
        IncumbentDevelopersStatisticsResult resp = statisticsController.getIncumbentDevelopersStatistics();
        assertNotNull(resp);
        assertEquals(expectedSize, resp.getIncumbentDevelopersStatisticsResult().size());
        assertEquals(expectedCount, resp.getIncumbentDevelopersStatisticsResult().get(0).getIncumbentCount());
    }

    /**
     * Get statistics for listings data.
     */
    @Test
    @Transactional
    public void retrieveListingCountStats() {
        final int expectedSize = 1;
        final Long expectedCount = 4L;
        ListingCountStatisticsResult resp = statisticsController.getListingCountStatistics();
        assertNotNull(resp);
        assertEquals(expectedSize, resp.getStatisticsResult().size());
        assertEquals(expectedCount, resp.getStatisticsResult().get(0).getDeveloperCount());
    }
}
