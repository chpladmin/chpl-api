package gov.healthit.chpl.app.chartdata;

import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.scheduler.job.chartdata.NonconformityTypeChartCalculator;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class NonconformityTypeChartDataAppTest extends TestCase {

    @Autowired
    private SurveillanceStatisticsDAO statisticsDAO;

    @Autowired
    private NonconformityTypeStatisticsDAO nonconformStatDAO;

    @Autowired
    private JpaTransactionManager txnManager;

    @Test
    @Transactional
    public void buildNonconformityTypeStatistics() {
        NonconformityTypeChartCalculator calc = new NonconformityTypeChartCalculator(statisticsDAO, nonconformStatDAO,
                txnManager);
        List<NonconformityTypeStatisticsDTO> dtos = calc.getCounts();
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
    }
}
