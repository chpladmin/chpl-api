package gov.healthit.chpl.manager.impl;


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

import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import junit.framework.TestCase;


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
	
}
