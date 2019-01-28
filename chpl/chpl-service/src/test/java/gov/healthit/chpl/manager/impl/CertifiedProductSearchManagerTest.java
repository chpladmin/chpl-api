package gov.healthit.chpl.manager.impl;

import java.util.List;

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
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
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
public class CertifiedProductSearchManagerTest extends TestCase {

    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void testSearchDeveloper() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDeveloper("Test Developer 1");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(10, response.getRecordCount().intValue());
        assertEquals(10, response.getResults().size());

        for (CertifiedProductBasicSearchResult result : response.getResults()) {
            assertTrue(result.getDeveloper().startsWith("Test Developer 1"));
        }
    }

    @Test
    @Transactional
    public void testSearchProduct() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setProduct("Test Product 1");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(6, response.getRecordCount().intValue());
        assertEquals(6, response.getResults().size());

        for (CertifiedProductBasicSearchResult result : response.getResults()) {
            assertTrue(result.getProduct().startsWith("Test Product 1"));
        }
    }

    @Test
    @Transactional
    public void testSearchVersion() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setVersion("1.0.1");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(2, response.getRecordCount().intValue());
        assertEquals(2, response.getResults().size());

        for (CertifiedProductBasicSearchResult result : response.getResults()) {
            assertTrue(result.getVersion().startsWith("1.0.1"));
        }
    }

    @Test
    @Transactional
    public void testSearchCertificationEdition() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.getCertificationEditions().add("2014");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(3, response.getRecordCount().intValue());
        assertEquals(3, response.getResults().size());

        for (CertifiedProductBasicSearchResult result : response.getResults()) {
            assertTrue(result.getEdition().toString().startsWith("2014"));
        }
    }

    @Test
    @Transactional
    public void testSearchCertificationBody() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.getCertificationBodies().add("InfoGard");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(7, response.getRecordCount().intValue());
        assertEquals(7, response.getResults().size());

        for (CertifiedProductBasicSearchResult result : response.getResults()) {
            assertTrue(result.getAcb().startsWith("InfoGard"));
        }
    }

    @Test
    @Transactional
    public void testSearchPracticeType() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPracticeType("Ambulatory");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(7, response.getRecordCount().intValue());
        assertEquals(7, response.getResults().size());

        for (CertifiedProductBasicSearchResult result : response.getResults()) {
            assertTrue(result.getPracticeType().startsWith("Ambulatory"));
        }
    }

    @Test
    @Transactional
    public void testSearchCertificationDateRangeStartDateOnly() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setCertificationDateStart("2015-08-20");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(16, response.getRecordCount().intValue());
        assertEquals(16, response.getResults().size());

        boolean foundFirstProduct = false;
        boolean foundSecondProduct = false;
        for (CertifiedProductBasicSearchResult result : response.getResults()) {
            if (result.getId().longValue() == 1L) {
                foundFirstProduct = true;
            }
            if (result.getId().longValue() == 2L) {
                foundSecondProduct = true;
            }
        }
        assertTrue(foundFirstProduct && foundSecondProduct);
    }

    @Test
    @Transactional
    public void testSearchCertificationDateRangeEndDateOnly() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setCertificationDateEnd("2015-08-20");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(2, response.getResults().size());
    }

    @Test
    @Transactional
    public void testSearchCertificationDateRangeStartAndEndDate() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setCertificationDateStart("2015-08-01");
        searchRequest.setCertificationDateEnd("2015-10-31");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(18, response.getResults().size());
    }

    @Test
    @Transactional
    public void testSearchCertificationDateRangeStartDateOnlyNoResults() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setCertificationDateStart("2015-12-20");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(0, response.getResults().size());
    }

    @Test
    @Transactional
    public void testSearchCertificationDateRangeEndDateOnlyNoResults() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setCertificationDateEnd("2015-01-20");
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(0, response.getResults().size());
    }

    @Test
    @Transactional
    public void testSearchVisibleOnCHPL() {

        SearchRequest searchRequest = new SearchRequest();
        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(18, response.getResults().size());
    }

    @Test
    @Transactional
    public void testSearch() {

        SearchRequest searchRequest = new SearchRequest();

        searchRequest.setSearchTerm("Test");
        searchRequest.setDeveloper("Test Developer");
        searchRequest.setProduct("Test");
        searchRequest.setVersion("2.0");
        searchRequest.getCertificationEditions().add("2014");
        searchRequest.getCertificationBodies().add("InfoGard");
        searchRequest.setPracticeType("Ambulatory");
        searchRequest.setOrderBy("product");
        searchRequest.setSortDescending(true);
        searchRequest.setPageNumber(0);

        SearchResponse response = certifiedProductSearchManager.search(searchRequest);
        assertEquals(1, response.getResults().size());

    }

    @Test
    @Transactional(readOnly = true)
    public void testBasicSearch() {
        List<CertifiedProductFlatSearchResult> response = certifiedProductSearchManager.search();

        assertNotNull(response);
        assertNotNull(response);
        assertEquals(18, response.size());

        boolean checkedCriteria = false;
        boolean checkedCqms = false;
        for (gov.healthit.chpl.domain.search.CertifiedProductSearchResult result : response) {
            if (result instanceof CertifiedProductBasicSearchResult) {
                CertifiedProductBasicSearchResult basicResult = (CertifiedProductBasicSearchResult) result;
                if (result.getId().longValue() == 1L) {
                    checkedCriteria = true;
                    assertNotNull(basicResult.getCriteriaMet().size());
                    assertEquals(4, basicResult.getCriteriaMet().size());
                }
                if (result.getId().longValue() == 2L) {
                    checkedCqms = true;
                    assertNotNull(basicResult.getCqmsMet().size());
                    assertEquals(2, basicResult.getCqmsMet().size());
                }
            } else if (result instanceof CertifiedProductFlatSearchResult) {
                CertifiedProductFlatSearchResult flatResult = (CertifiedProductFlatSearchResult) result;
                if (result.getId().longValue() == 1L) {
                    checkedCriteria = true;
                    assertNotNull(flatResult.getCriteriaMet());
                    assertTrue(flatResult.getCriteriaMet().length() > 0);
                }
                if (result.getId().longValue() == 2L) {
                    checkedCqms = true;
                    assertNotNull(flatResult.getCqmsMet());
                    assertTrue(flatResult.getCqmsMet().length() > 0);
                }
            }
        }
        assertTrue(checkedCriteria);
        assertTrue(checkedCqms);
    }
}
