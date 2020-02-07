package old.gov.healthit.chpl.app.chartdata;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.scheduler.job.chartdata.CriterionProductStatisticsCalculator;
import junit.framework.TestCase;
import old.gov.healthit.chpl.CHPLTestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CriterionProductStatisticsChartDataAppTest extends TestCase {

    private static final int EXPECTED_LENGTH = 12;
    private static final Long EXPECTED_PRODUCT_COUNT = 2L;
    private static final String CRITERIA = "170.315 (a)(1)";

    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDAO;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

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
    public void buildListingCountStatistics() {
        CriterionProductStatisticsCalculator calc = new CriterionProductStatisticsCalculator();

        List<CertifiedProductFlatSearchResult> allResults = certifiedProductSearchDAO.getAllCertifiedProducts();
        Map<String, Long> results = calc.getCounts(allResults);

        assertNotNull(results);
        assertEquals(EXPECTED_LENGTH, results.size());
        assertEquals(EXPECTED_PRODUCT_COUNT, results.get(CRITERIA));
    }
}
