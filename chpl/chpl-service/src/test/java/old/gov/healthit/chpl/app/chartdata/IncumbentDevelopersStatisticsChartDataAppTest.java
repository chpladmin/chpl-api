package old.gov.healthit.chpl.app.chartdata;

import java.util.List;

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
import gov.healthit.chpl.dao.IncumbentDevelopersStatisticsDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.scheduler.job.chartdata.IncumbentDevelopersStatisticsCalculator;
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
public class IncumbentDevelopersStatisticsChartDataAppTest extends TestCase {

    private static final int STARTING_LENGTH = 1;
    private static final Long STARTING_NEW_COUNT = 3L;
    private static final Long STARTING_INCUMBENT_COUNT = 4L;
    private static final int ENDING_LENGTH = 3;
    private static final Long ENDING_NEW_COUNT = 2L;
    private static final Long ENDING_INCUMBENT_COUNT = 1L;

    @Autowired
    private IncumbentDevelopersStatisticsDAO statisticsDAO;
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
    public void buildIncumbentDeveloperStatistics() {
        IncumbentDevelopersStatisticsCalculator calc = new IncumbentDevelopersStatisticsCalculator();
        List<IncumbentDevelopersStatisticsDTO> dtos = statisticsDAO.findAll();
        assertNotNull(dtos);
        assertEquals(STARTING_LENGTH, dtos.size());
        assertEquals(STARTING_INCUMBENT_COUNT, dtos.get(0).getIncumbentCount());
        assertEquals(STARTING_NEW_COUNT, dtos.get(0).getNewCount());

        List<CertifiedProductFlatSearchResult> allResults = certifiedProductSearchDAO.getAllCertifiedProducts();
        dtos = calc.getCounts(allResults);

        assertNotNull(dtos);
        assertEquals(ENDING_LENGTH, dtos.size());
        for (IncumbentDevelopersStatisticsDTO dto : dtos) {
            if (dto.getNewCertificationEditionId().longValue() == 3
                    && dto.getOldCertificationEditionId().longValue() == 2) {
                assertEquals(3, dto.getNewCount().longValue());
                assertEquals(1, dto.getIncumbentCount().longValue());
            } else if (dto.getNewCertificationEditionId().longValue() == 3
                    && dto.getOldCertificationEditionId().longValue() == 1) {
                assertEquals(2, dto.getNewCount().longValue());
                assertEquals(2, dto.getIncumbentCount().longValue());
            } else if (dto.getNewCertificationEditionId().longValue() == 2
                    && dto.getOldCertificationEditionId().longValue() == 1) {
                assertEquals(0, dto.getNewCount().longValue());
                assertEquals(1, dto.getIncumbentCount().longValue());
            }
        }
    }
}
