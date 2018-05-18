package gov.healthit.chpl.web.controller;

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
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import gov.healthit.chpl.web.controller.results.IncumbentDevelopersStatisticsResult;
import junit.framework.TestCase;

/**
 * Tests for Statistics controller.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
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
        assertEquals(expectedCount, resp.getCriterionProductStatisticsResult().size());
        assertEquals("170.314 (a)(14)", resp.getCriterionProductStatisticsResult().get(0).getCriterion().getNumber());
    }

    /**
     * Get statistics for incumbent developers data.
     */
    @Test
    @Transactional
    public void retrieveNewVsIncumbentDeveloperStats() {
        final Long expectedCount = 3L;
        IncumbentDevelopersStatisticsResult resp = statisticsController.getIncumbentDevelopersStatistics();
        assertNotNull(resp);
        assertEquals(expectedCount, resp.getIncumbentDevelopersStatisticsResult()
                .get(0).getNew2011To2014());
    }
}
