package gov.healthit.chpl.app.chartdata;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.CHPLTestConfig;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.scheduler.job.chartdata.NonconformityTypeChartCalculator;
import junit.framework.TestCase;

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

    /**
     * create an application context that the statistics calculator requires
     * @throws ServletException
     */
    @BeforeClass
    public static void setup() throws ServletException {
        AnnotationConfigWebApplicationContext wac = new AnnotationConfigWebApplicationContext();
        wac.register(CHPLTestConfig.class);
        MockServletContext sc = new MockServletContext("");
        ServletContextListener listener = new ContextLoaderListener(wac);
        ServletContextEvent event = new ServletContextEvent(sc);
        listener.contextInitialized(event);
    }

    @Test
    @Transactional
    public void buildNonconformityTypeStatistics() {
        NonconformityTypeChartCalculator calc = new NonconformityTypeChartCalculator();
        List<NonconformityTypeStatisticsDTO> dtos = calc.getCounts();
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
    }
}
