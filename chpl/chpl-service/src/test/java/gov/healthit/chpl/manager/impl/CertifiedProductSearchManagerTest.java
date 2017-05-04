package gov.healthit.chpl.manager.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheUpdater;
import gov.healthit.chpl.caching.CacheUtil;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import junit.framework.TestCase;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductSearchManagerTest extends TestCase {
	
	@Autowired
	private CertifiedProductSearchManager certifiedProductSearchManager;
	
	@Autowired private CacheUtil cacheUtil;
	
	@Autowired private CacheUpdater cacheUpdater;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@Test
	@Transactional
	public void testSearchDeveloper(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setDeveloper("Test Developer 1");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(5, response.getResults().size());
		
		for (CertifiedProductSearchResult result : response.getResults() ){
			assertTrue(result.getDeveloper().get("name").toString().startsWith("Test Developer 1"));
		}
	}
	
	@Test
	@Transactional
	public void testSearchProduct(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setProduct("Test Product 1");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(3, response.getResults().size());
		
		for (CertifiedProductSearchResult result : response.getResults() ){
			assertTrue(result.getProduct().get("name").toString().startsWith("Test Product 1"));
		}
	}
	
	@Test
	@Transactional
	public void testSearchVersion(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setVersion("1.0.1");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(3, response.getResults().size());
		
		for (CertifiedProductSearchResult result : response.getResults() ){
			assertTrue(result.getProduct().get("version").toString().startsWith("1.0.1"));
		}
	}
	
	@Test
	@Transactional
	public void testSearchCertificationEdition(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.getCertificationEditions().add("2014");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(2, response.getResults().size());
		
		for (CertifiedProductSearchResult result : response.getResults() ){
			assertTrue(result.getCertificationEdition().get("name").toString().startsWith("2014"));
		}
	}
	
	@Test
	@Transactional
	public void testSearchCertificationBody(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.getCertificationBodies().add("InfoGard");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(4, response.getResults().size());
		
		for (CertifiedProductSearchResult result : response.getResults() ){
			assertTrue(result.getCertifyingBody().get("name").toString().startsWith("InfoGard"));
		}
	}

	@Test
	@Transactional
	public void testSearchPracticeType(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setPracticeType("Ambulatory");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(4, response.getResults().size());
		
		for (CertifiedProductSearchResult result : response.getResults() ){
			assertTrue(result.getPracticeType().get("name").toString().startsWith("Ambulatory"));
		}
	}
	
	@Test
	@Transactional
	public void testSearchCertificationDateRangeStartDateOnly(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setCertificationDateStart("2015-08-20");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(11, response.getResults().size());
		
		boolean foundFirstProduct = false;
		boolean foundSecondProduct = false;
		for (CertifiedProductSearchResult result : response.getResults() ){
			if(result.getId().longValue() == 1L) {
				foundFirstProduct = true;
			}
			if(result.getId().longValue() == 2L) {
				foundSecondProduct = true;
			}
		}
		assertTrue(foundFirstProduct && foundSecondProduct);
	}
	
	@Test
	@Transactional
	public void testSearchCertificationDateRangeEndDateOnly(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setCertificationDateEnd("2015-08-20");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(12, response.getResults().size());
	}
	
	@Test
	@Transactional
	public void testSearchCertificationDateRangeStartAndEndDate(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setCertificationDateStart("2015-08-01");
		searchRequest.setCertificationDateEnd("2015-10-31");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(12, response.getResults().size());
	}
	
	@Test
	@Transactional
	public void testSearchCertificationDateRangeStartDateOnlyNoResults(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setCertificationDateStart("2015-12-20");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(0, response.getResults().size());
	}
	
	@Test
	@Transactional
	public void testSearchCertificationDateRangeEndDateOnlyNoResults(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setCertificationDateEnd("2015-01-20");
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(0, response.getResults().size());
	}
	
	@Test
	@Transactional
	public void testSearchVisibleOnCHPL(){
		
		SearchRequest searchRequest = new SearchRequest();
		SearchResponse response = certifiedProductSearchManager.search(searchRequest);
		assertEquals(12, response.getResults().size());
	}
	
	@Test
	@Transactional
	public void testSearch(){
		
		SearchRequest searchRequest = new SearchRequest();
		
		searchRequest.setSearchTerm("Test");
		searchRequest.setDeveloper("Test Developer");
		searchRequest.setProduct("Test");
		searchRequest.setVersion("1.0.1");
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
		assertEquals(16, response.size());
		
		boolean checkedCriteria = false;
		boolean checkedCqms = false;
		for(gov.healthit.chpl.domain.search.CertifiedProductSearchResult result : response) {
			if(result instanceof CertifiedProductBasicSearchResult) {
				CertifiedProductBasicSearchResult basicResult = (CertifiedProductBasicSearchResult) result;
				if(result.getId().longValue() == 1L) {
					checkedCriteria = true;
					assertNotNull(basicResult.getCriteriaMet().size());
					assertEquals(4, basicResult.getCriteriaMet().size());
				}
				if(result.getId().longValue() == 2L) {
					checkedCqms = true;
					assertNotNull(basicResult.getCqmsMet().size());
					assertEquals(2, basicResult.getCqmsMet().size());
				}
			} else if(result instanceof CertifiedProductFlatSearchResult) {
				CertifiedProductFlatSearchResult flatResult = (CertifiedProductFlatSearchResult) result;
				if(result.getId().longValue() == 1L) {
					checkedCriteria = true;
					assertNotNull(flatResult.getCriteriaMet());
					assertTrue(flatResult.getCriteriaMet().length() > 0);
				}
				if(result.getId().longValue() == 2L) {
					checkedCqms = true;
					assertNotNull(flatResult.getCqmsMet());
					assertTrue(flatResult.getCqmsMet().length() > 0);
				}
			}
		}
		assertTrue(checkedCriteria);
		assertTrue(checkedCqms);
	}
	

	@Test
	@Transactional(readOnly = true)
	public void testBasicSearchCache() throws IllegalStateException, CacheException, ClassCastException, InterruptedException, ExecutionException {
		CacheManager manager = cacheUtil.getMyCacheManager();
		assertEquals(0, manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH).getSize());
		assertEquals(0, manager.getCache(CacheNames.BASIC_SEARCH).getSize());
		List<CertifiedProductFlatSearchResult> response = certifiedProductSearchManager.search();
		//basic search response should now be cached
		List<CertifiedProductFlatSearchResult> response2 = certifiedProductSearchManager.search();
		//responses should be the same object
		assertEquals(response, response2);
		
		//expect cache to clear properly the first time; prefetched cache was not cached
		Future<Boolean> isEvictDone = cacheUpdater.updateBasicSearch(); 
		isEvictDone.get();
		assertTrue(manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH).getSize() > 0);
		assertTrue(manager.getCache(CacheNames.BASIC_SEARCH).getSize() > 0);
		// Verify that the preFetchedCache gets populated
		assertTrue(manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH).getSize() > 0);
		assertTrue(manager.getCache(CacheNames.BASIC_SEARCH).getSize() > 0);
		List<CertifiedProductFlatSearchResult> response3 = certifiedProductSearchManager.search();
		assertTrue(manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH).getSize() > 0);
		assertTrue(manager.getCache(CacheNames.BASIC_SEARCH).getSize() > 0);
		assertNotSame(response2, response3); 
		
		//make sure the cache also clears the second time; prefetched cache was cached
		isEvictDone = cacheUpdater.updateBasicSearch();
		isEvictDone.get();
		assertTrue(manager.getCache(CacheNames.BASIC_SEARCH).getSize() > 0);
		assertTrue(manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH).getSize() > 0);
		// Verify that the preFetchedCache gets populated
		assertTrue(manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH).getSize() > 0);
		assertTrue(manager.getCache(CacheNames.BASIC_SEARCH).getSize() > 0);
		List<CertifiedProductFlatSearchResult> response4 = certifiedProductSearchManager.search();
		assertTrue(manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH).getSize() > 0);
		assertTrue(manager.getCache(CacheNames.BASIC_SEARCH).getSize() > 0);
		assertNotSame(response3, response4);
	}
}
