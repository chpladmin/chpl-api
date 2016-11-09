package gov.healthit.chpl.dao.impl;

import java.util.Collection;
import java.util.List;

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

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductSearchResultDaoTest extends TestCase {

	@Autowired
	private CertifiedProductSearchResultDAO searchResultDAO;
	
	
//	@Test
//	public void testGetDownloadResults() throws EntityRetrievalException {
//		CertifiedProductDetailsDTO result = searchResultDAO.getAllDetailsById(1L);
//		assertNotNull(result);
//		assertNotNull(result.getCqmResults());
//		assertNotNull(result.getCertResults());
//		assertEquals(3, result.getCqmResults().size());
//	}
	
	@Test
	@Transactional
	public void testCountSearchResults(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setDeveloper("Test");
		Long countProducts = searchResultDAO.countMultiFilterSearchResults(searchRequest);
		assertEquals(5, countProducts.intValue());
		
		searchRequest.setVersion("1.0.0");
		Long countProductsVersionSpecific = searchResultDAO.countMultiFilterSearchResults(searchRequest);
		assertEquals(2, countProductsVersionSpecific.intValue());
	}
	
	@Test
	@Transactional
	public void testSearchDeveloper(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setDeveloper("Test Developer 1");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(4, products.size());
		
		for (CertifiedProductDetailsDTO dto : products ){
			assertTrue(dto.getDeveloper().getName().startsWith("Test Developer 1"));
		}
	}
	
	@Test
	@Transactional
	public void testSearchProduct(){
		
		SearchRequest searchRequest = new SearchRequest();
		
		searchRequest.setProduct("Test Product 1");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(4, products.size());
		
		for (CertifiedProductDetailsDTO dto : products ){
			assertTrue(dto.getProduct().getName().startsWith("Test Product 1"));
		}
		
	}
	
	@Test
	@Transactional
	public void testSearchVersion(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setVersion("1.0.1");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		for (CertifiedProductDetailsDTO dto : products ){
			assertTrue(dto.getVersion().getVersion().startsWith("1.0.1"));
		}
		
	}
	
	@Test
	@Transactional
	public void testSearchCertificationEdition(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.getCertificationEditions().add("2014");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(2, products.size());
		
		for (CertifiedProductDetailsDTO dto : products ){
			assertTrue(dto.getYear().startsWith("2014"));
		}
	}
	
	@Test
	@Transactional
	public void testSearchCertificationBody(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.getCertificationBodies().add("InfoGard");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(5, products.size());
		
		for (CertifiedProductDetailsDTO dto : products ){
			assertTrue(dto.getCertificationBodyName().startsWith("InfoGard"));
		}
		
	}
	
	@Test
	@Transactional
	public void testSearchPracticeType(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setPracticeType("Ambulatory");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(5, products.size());
		
		for (CertifiedProductDetailsDTO dto : products ){
			assertTrue(dto.getPracticeTypeName().startsWith("Ambulatory"));
		}
		
	}
	
	@Test
	@Transactional
	public void testSearchVisibleOnCHPL(){
		
		SearchRequest searchRequest = new SearchRequest();
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(5, products.size());
	}
	
	@Test
	@Transactional
	public void testSearchHasCAP(){
		
		SearchRequest searchRequest = new SearchRequest();
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(5, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getCorrectiveActionPlans().add("open");
		products = searchResultDAO.search(searchRequest);
		assertEquals(5, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getCorrectiveActionPlans().add("closed");
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getCorrectiveActionPlans().add("never");
		products = searchResultDAO.search(searchRequest);
		assertEquals(0, products.size());
	}
	
	
	
	@Test
	@Transactional
	public void testSearch(){
		
		SearchRequest searchRequest = new SearchRequest();
		
		searchRequest.setSearchTerm("Test");
		searchRequest.setDeveloper("Test Developer 1");
		searchRequest.setProduct("Test");
		searchRequest.setVersion("1.0.1");
		searchRequest.getCertificationEditions().add("2014");
		searchRequest.getCertificationBodies().add("InfoGard");
		searchRequest.setPracticeType("Ambulatory");
		searchRequest.setOrderBy("product");
		searchRequest.setSortDescending(true);
		searchRequest.setPageNumber(0);
		
		
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
	}
	
	@Test
	@Transactional
	public void testFetchSingleItem(){
		
		try {
			CertifiedProductDetailsDTO product = searchResultDAO.getById(1L);
			
			assertEquals(-1, product.getCertificationBodyId().intValue());
			assertEquals("InfoGard", product.getCertificationBodyName());
			assertEquals("CHP-024050",product.getChplProductNumber());
			assertEquals(2, product.getCertificationEditionId().intValue());
			assertEquals("Test Developer 1", product.getDeveloper().getName());
			assertEquals(3, product.getCountCertifications().intValue());
			assertEquals(0, product.getCountCqms().intValue());
			
		} catch (EntityRetrievalException e) {
			fail("EntityRetrievalException");
		}
		
	}
	
	
}