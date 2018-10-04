package gov.healthit.chpl.app.chartdata;

import java.util.List;

import org.junit.Rule;
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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.ListingCountStatisticsDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.scheduler.job.chartdata.ListingCountStatisticsCalculator;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ListingCountStatisticsChartDataAppTest extends TestCase {

    private static final int STARTING_LENGTH = 1;
    private static final Long STARTING_PRODUCT_COUNT = 3L;
    private static final Long STARTING_DEVELOPER_COUNT = 4L;
    private static final int ENDING_LENGTH = 7;
    private static final Long ENDING_PRODUCT_COUNT = 1L;
    private static final Long ENDING_DEVELOPER_COUNT = 1L;

    @Autowired
    private ListingCountStatisticsDAO statisticsDAO;

    @Autowired
    private CertificationEditionDAO certificationEditionDAO;

    @Autowired
    private CertificationStatusDAO certificationStatusDAO;

    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDAO;

    @Autowired
    private JpaTransactionManager txnManager;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void buildListingCountStatistics() {
        ListingCountStatisticsCalculator calc = new ListingCountStatisticsCalculator();
        List<ListingCountStatisticsDTO> dtos = statisticsDAO.findAll();
        assertNotNull(dtos);
        assertEquals(STARTING_LENGTH, dtos.size());
        assertEquals(STARTING_DEVELOPER_COUNT, dtos.get(0).getDeveloperCount());
        assertEquals(STARTING_PRODUCT_COUNT, dtos.get(0).getProductCount());

        List<CertifiedProductFlatSearchResult> allResults = certifiedProductSearchDAO.getAllCertifiedProducts();
        dtos = calc.getCounts(allResults);

        assertNotNull(dtos);
        assertEquals(ENDING_LENGTH, dtos.size());
        assertEquals(ENDING_DEVELOPER_COUNT, dtos.get(0).getDeveloperCount());
        assertEquals(ENDING_PRODUCT_COUNT, dtos.get(0).getProductCount());
    }
}
