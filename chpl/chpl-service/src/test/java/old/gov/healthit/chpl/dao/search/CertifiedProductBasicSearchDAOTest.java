package old.gov.healthit.chpl.dao.search;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
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
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductBasicSearchDAOTest extends TestCase {

    @Autowired
    private CertifiedProductSearchDAO cpSearchDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Test
    @Transactional(readOnly = true)
    public void getAllCertifiedProducts() {
        Date startDate = new Date();
        List<CertifiedProductFlatSearchResult> results = cpSearchDao.getAllCertifiedProducts();
        Date endDate = new Date();
        System.out.println("Search took " + ((endDate.getTime() - startDate.getTime()) / 1000) + " seconds");

        assertNotNull(results);
        assertEquals(18, results.size());

        boolean checkedCriteria = false;
        boolean checkedCqms = false;
        for (CertifiedProductFlatSearchResult result : results) {
            if (result.getId().longValue() == 1L) {
                checkedCriteria = true;
                assertNotNull(result.getCriteriaMet());
                assertTrue(result.getCriteriaMet().length() > 0);
                // assertNotNull(result.getCriteriaMet());
                // assertEquals(4, result.getCriteriaMet().size());
            }
            if (result.getId().longValue() == 2L) {
                checkedCqms = true;
                assertNotNull(result.getCqmsMet());
                assertTrue(result.getCqmsMet().length() > 0);
                // assertNotNull(result.getCqmsMet());
                // assertEquals(2, result.getCqmsMet().size());
            }
        }
        assertTrue(checkedCriteria);
        assertTrue(checkedCqms);
    }
}
