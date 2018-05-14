package gov.healthit.chpl.manager.impl;

import java.io.IOException;

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

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.manager.StatisticsManager;
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class StatisticsManagerTest extends TestCase {

    @Autowired
    private StatisticsManager statisticsManager;

    @Test
    @Transactional
    public void testFuzzyChoice() throws EntityCreationException, EntityRetrievalException, IOException{
        CriterionProductStatisticsResult stats = statisticsManager.getCriterionProductStatisticsResult();
        assertNotNull(stats);
        assertEquals("170.315 (d)(10)", stats.getCriterionProductStatisticsResult().get(0).getCriterion().getNumber());
    }
}
