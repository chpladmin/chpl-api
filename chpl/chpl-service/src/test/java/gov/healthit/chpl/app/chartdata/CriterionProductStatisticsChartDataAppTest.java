package gov.healthit.chpl.app.chartdata;

import java.util.List;
import java.util.Map;

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
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CriterionProductStatisticsChartDataAppTest extends TestCase {

    private static final int EXPECTED_LENGTH = 5;
    private static final Long EXPECTED_PRODUCT_COUNT = 2L;
    private static final String CRITERIA = "170.315 (a)(1)";

    @Autowired
    private CriterionProductStatisticsDAO statisticsDAO;

    @Autowired
    private CertificationCriterionDAO certificationCriterionDAO;

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
        CriterionProductStatisticsCalculator calc = new CriterionProductStatisticsCalculator(
                statisticsDAO, certificationCriterionDAO, txnManager);

        List<CertifiedProductFlatSearchResult> allResults = certifiedProductSearchDAO.getAllCertifiedProducts();
        Map<String, Long> results = calc.getCounts(allResults);

        assertNotNull(results);
        assertEquals(EXPECTED_LENGTH, results.size());
        assertEquals(EXPECTED_PRODUCT_COUNT, results.get(CRITERIA));
    }
}
