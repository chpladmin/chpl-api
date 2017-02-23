package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceSearchOptions;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
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
	@Autowired
	private SurveillanceDAO survDao;
	@Autowired
	private CertifiedProductDAO cpDao;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	private static JWTAuthenticatedUser adminUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	@Test
	@Transactional
	public void testCountSearchResults(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setDeveloper("Test");
		Long countProducts = searchResultDAO.countMultiFilterSearchResults(searchRequest);
		assertEquals(5, countProducts.intValue());
		
		searchRequest.setVersion("1.0.0");
		Long countProductsVersionSpecific = searchResultDAO.countMultiFilterSearchResults(searchRequest);
		assertEquals(1, countProductsVersionSpecific.intValue());
	}
	
	@Test
	@Transactional
	public void testSearchDeveloper(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setDeveloper("Test Developer 1");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(5, products.size());
		
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
		assertEquals(3, products.size());
		
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
		assertEquals(3, products.size());
		
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
		assertEquals(4, products.size());
		
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
		assertEquals(4, products.size());
		
		for (CertifiedProductDetailsDTO dto : products ){
			assertTrue(dto.getPracticeTypeName().startsWith("Ambulatory"));
		}
		
	}
	
	@Test
	@Transactional
	public void testSearchVisibleOnCHPL(){
		
		SearchRequest searchRequest = new SearchRequest();
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(12, products.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSearchActiveSurveillanceWithoutNonconformities() throws EntityRetrievalException, UserPermissionRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Long insertedId = survDao.insertSurveillance(surv);
		assertNotNull(insertedId);
		SecurityContextHolder.getContext().setAuthentication(null);
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setHasHadSurveillance(true);
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(2, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.setHasHadSurveillance(false);
		products = searchResultDAO.search(searchRequest);
		assertEquals(10, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_NONCONFORMITY);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_NONCONFORMITY);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_SURVEILLANCE);
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(0, products.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSearchClosedSurveillanceWithoutNonconformities() throws EntityRetrievalException, UserPermissionRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date(System.currentTimeMillis() - (7*24*60*60*1000)));
		surv.setEndDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Long insertedId = survDao.insertSurveillance(surv);
		assertNotNull(insertedId);
		SecurityContextHolder.getContext().setAuthentication(null);
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setHasHadSurveillance(true);
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(2, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.setHasHadSurveillance(false);
		products = searchResultDAO.search(searchRequest);
		assertEquals(10, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_NONCONFORMITY);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_NONCONFORMITY);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(0, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(2, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_SURVEILLANCE);
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(0, products.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSearchActiveSurveillanceWithNonconformities() throws EntityRetrievalException, UserPermissionRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		surv.getRequirements().add(req);

		SurveillanceRequirement req2 = new SurveillanceRequirement();
		req2.setRequirement("170.314 (a)(2)");
		reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req2.setType(reqType);
		resType = survDao.findSurveillanceResultType("Non-Conformity");
		req2.setResult(resType);
		surv.getRequirements().add(req2);
		
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setCapApprovalDate(new Date());
		nc.setCapMustCompleteDate(new Date());
		nc.setCapStartDate(new Date());
		nc.setDateOfDetermination(new Date());
		nc.setDeveloperExplanation("Something");
		nc.setFindings("Findings!");
		nc.setSitesPassed(2);
		nc.setNonconformityType("170.314 (a)(2)");
		nc.setSummary("summary");
		nc.setTotalSites(5);
		SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType("Open");
		nc.setStatus(ncStatus);
		req2.getNonconformities().add(nc);
		
		Long insertedId = survDao.insertSurveillance(surv);
		assertNotNull(insertedId);
		SecurityContextHolder.getContext().setAuthentication(null);
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setHasHadSurveillance(true);
		List<CertifiedProductDetailsDTO> products = searchResultDAO.search(searchRequest);
		assertEquals(2, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.setHasHadSurveillance(false);
		products = searchResultDAO.search(searchRequest);
		assertEquals(10, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_NONCONFORMITY);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_NONCONFORMITY);
		products = searchResultDAO.search(searchRequest);
		assertEquals(2, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_SURVEILLANCE);
		products = searchResultDAO.search(searchRequest);
		assertEquals(1, products.size());
		
		searchRequest = new SearchRequest();
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.CLOSED_SURVEILLANCE);
		searchRequest.getSurveillance().add(SurveillanceSearchOptions.OPEN_SURVEILLANCE);
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
			assertEquals(4, product.getCountCertifications().intValue());
			assertEquals(0, product.getCountCqms().intValue());
			
		} catch (EntityRetrievalException e) {
			fail("EntityRetrievalException");
		}
		
	}
	
	/** 
	 * Tests that the default /search call caches its data
	 */
	@Transactional
	@Test
	public void test_cpDetailsSearch_CachesData() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SearchRequest searchFilters = new SearchRequest();
		List<String> certBodies = new ArrayList<String>();
		certBodies.add("Drummond Group");
		certBodies.add("ICSA Labs");
		certBodies.add("InfoGard");
		List<String> certCriteria = new ArrayList<String>();
		List<String> certEditions = new ArrayList<String>();
		certEditions.add("2014");
		certEditions.add("2015");
		List<String> certStatuses = new ArrayList<String>();
		certStatuses.add("Active");
		certStatuses.add("Suspended by ONC-ACB");
		certStatuses.add("Withdrawn by Developer");
		certStatuses.add("Withdrawn by ONC-ACB");
		certStatuses.add("Suspended by ONC");
		certStatuses.add("Terminated by ONC");
		List<String> cqms = new ArrayList<String>();
		Set<SurveillanceSearchOptions> survs = new HashSet<SurveillanceSearchOptions>();
		 
		searchFilters.setCertificationBodies(certBodies);
		searchFilters.setCertificationCriteria(certCriteria);
		searchFilters.setCertificationDateEnd(null);
		searchFilters.setCertificationDateStart(null);
		searchFilters.setCertificationEditions(certEditions);
		searchFilters.setCertificationStatuses(certStatuses);
		searchFilters.setCqms(cqms);
		searchFilters.setDeveloper(null);
		searchFilters.setHasHadSurveillance(null);
		searchFilters.setOrderBy("developer");
		searchFilters.setPageNumber(0);
		searchFilters.setPageSize(50);
		searchFilters.setPracticeType(null);
		searchFilters.setProduct(null);
		searchFilters.setSearchTerm(null);
		searchFilters.setSortDescending(false);
		searchFilters.setSurveillance(survs);
		searchFilters.setVersion(null);
		long startTime = System.currentTimeMillis();
		List<CertifiedProductDetailsDTO> results = searchResultDAO.search(searchFilters);
		// search should now be cached
		long endTime = System.currentTimeMillis();
		long timeLength = endTime - startTime;
		double elapsedSecs = timeLength / 1000.0;
		
		assertTrue("Returned " + results.size() + " results but should return more than 0", results.size() > 0);
		
		System.out.println("search returned " + results.size() + " total search results.");
		System.out.println("search completed in  " + timeLength
				+ " millis or " + elapsedSecs + " seconds");
		
		// now compare cached time vs non-cached time
		startTime = System.currentTimeMillis();
		results = searchResultDAO.search(searchFilters);
		endTime = System.currentTimeMillis();
		timeLength = endTime - startTime;
		elapsedSecs = timeLength / 1000.0;
		System.out.println("search returned " + results.size() + " total search results.");
		System.out.println("search completed in  " + timeLength
				+ " millis or " + elapsedSecs + " seconds");
		
		assertTrue("search should complete within 100 ms but took " + timeLength
				+ " millis or " + elapsedSecs + " seconds", timeLength < 100);
	}
	
	/** 
	 * Tests that the default /search call countMultiFilterSearchResults() caches its data
	 */
	@Transactional
	@Test
	public void test_countMultiFilterSearchResults_CachesData() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SearchRequest searchFilters = new SearchRequest();
		List<String> certBodies = new ArrayList<String>();
		certBodies.add("Drummond Group");
		certBodies.add("ICSA Labs");
		certBodies.add("InfoGard");
		List<String> certCriteria = new ArrayList<String>();
		List<String> certEditions = new ArrayList<String>();
		certEditions.add("2014");
		certEditions.add("2015");
		List<String> certStatuses = new ArrayList<String>();
		certStatuses.add("Active");
		certStatuses.add("Suspended by ONC-ACB");
		certStatuses.add("Withdrawn by Developer");
		certStatuses.add("Withdrawn by ONC-ACB");
		certStatuses.add("Suspended by ONC");
		certStatuses.add("Terminated by ONC");
		List<String> cqms = new ArrayList<String>();
		Set<SurveillanceSearchOptions> survs = new HashSet<SurveillanceSearchOptions>();
		 
		searchFilters.setCertificationBodies(certBodies);
		searchFilters.setCertificationCriteria(certCriteria);
		searchFilters.setCertificationDateEnd(null);
		searchFilters.setCertificationDateStart(null);
		searchFilters.setCertificationEditions(certEditions);
		searchFilters.setCertificationStatuses(certStatuses);
		searchFilters.setCqms(cqms);
		searchFilters.setDeveloper(null);
		searchFilters.setHasHadSurveillance(null);
		searchFilters.setOrderBy("developer");
		searchFilters.setPageNumber(0);
		searchFilters.setPageSize(50);
		searchFilters.setPracticeType(null);
		searchFilters.setProduct(null);
		searchFilters.setSearchTerm(null);
		searchFilters.setSortDescending(false);
		searchFilters.setSurveillance(survs);
		searchFilters.setVersion(null);
		long startTime = System.currentTimeMillis();
		Long result = searchResultDAO.countMultiFilterSearchResults(searchFilters);
		// search should now be cached
		long endTime = System.currentTimeMillis();
		long timeLength = endTime - startTime;
		double elapsedSecs = timeLength / 1000.0;
		
		assertTrue("Returned " + result + " which should have a count more than 0", result > 0);
		
		System.out.println("search completed in  " + timeLength
				+ " millis or " + elapsedSecs + " seconds");
		
		// now compare cached time vs non-cached time
		startTime = System.currentTimeMillis();
		result = searchResultDAO.countMultiFilterSearchResults(searchFilters);
		endTime = System.currentTimeMillis();
		timeLength = endTime - startTime;
		elapsedSecs = timeLength / 1000.0;
		System.out.println("search completed in  " + timeLength
				+ " millis or " + elapsedSecs + " seconds");
		
		assertTrue("search should complete within 100 ms but took " + timeLength
				+ " millis or " + elapsedSecs + " seconds", timeLength < 100);
	}
	
	
}