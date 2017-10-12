package gov.healthit.chpl.web.controller;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.search.BasicSearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class CollectionsControllerTest extends TestCase {
	@Autowired CollectionsController collectionsController;
	
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

	@Transactional
	@Test
	public void testBasicSearchDefaultViewHasRequiredFields() throws JsonProcessingException, EntityRetrievalException {
		String resp = collectionsController.getAllCertifiedProducts(null);
		ObjectMapper mapper = new ObjectMapper();
		BasicSearchResponse results = null;
		
		try {
			results = mapper.readValue(resp, BasicSearchResponse.class);
		} catch(Exception ex) {
			fail("Caught exception " + ex.getMessage());
		}
		
		assertNotNull(results);
		assertNotNull(results.getResults());
		assertEquals(16, results.getResults().size());
		
		//check that all fields are present for products in which we know those fields exist
		for(CertifiedProductFlatSearchResult result : results.getResults()) {
			assertNotNull(result.getId());
			assertNotNull(result.getChplProductNumber());
			assertNotNull(result.getEdition());
			if(result.getId().longValue() != 2) {
				assertNotNull(result.getAtl());
			}
			assertNotNull(result.getAcb());
			assertNotNull(result.getAcbCertificationId());
			assertNotNull(result.getDeveloper());
			assertNotNull(result.getProduct());
			assertNotNull(result.getVersion());
			assertNotNull(result.getCertificationDate());
			assertNotNull(result.getCertificationStatus());
			assertNull(result.getDecertificationDate());
			assertNull(result.getNumMeaningfulUse());
			assertNotNull(result.getSurveillanceCount());
			assertNotNull(result.getOpenNonconformityCount());
			assertNotNull(result.getClosedNonconformityCount());
			assertNotNull(result.getPracticeType());
			if(result.getId().longValue() == 1 || result.getId().longValue() == 2 ||
				result.getId().longValue() == 3 || result.getId().longValue() == 9) {
				assertNotNull(result.getPreviousDevelopers());
			}
			if(result.getId().longValue() == 1 || result.getId().longValue() == 2 || 
				result.getId().longValue() == 3 || result.getId().longValue() == 5 || 
				result.getId().longValue() == 10) {
				assertNotNull(result.getCriteriaMet());
			}
			if(result.getId().longValue() == 2) {
				assertNotNull(result.getCqmsMet());
			}
		}
	}
	
	@Transactional
	@Test
	public void testBasicSearchWithCustomFields() throws JsonProcessingException, EntityRetrievalException {
		String resp = collectionsController.getAllCertifiedProducts("id,chplProductNumber");
		ObjectMapper mapper = new ObjectMapper();
		BasicSearchResponse results = null;
		
		try {
			results = mapper.readValue(resp, BasicSearchResponse.class);
		} catch(Exception ex) {
			fail("Caught exception " + ex.getMessage());
		}
		
		assertNotNull(results);
		assertNotNull(results.getResults());
		assertEquals(16, results.getResults().size());
		
		for(CertifiedProductFlatSearchResult result : results.getResults()) {
			assertNotNull(result.getId());
			assertNotNull(result.getChplProductNumber());
			assertNull(result.getEdition());
			assertNull(result.getAtl());
			assertNull(result.getAcb());
			assertNull(result.getAcbCertificationId());
			assertNull(result.getDeveloper());
			assertNull(result.getProduct());
			assertNull(result.getVersion());
			assertNull(result.getCertificationDate());
			assertNull(result.getCertificationStatus());
			assertNull(result.getDecertificationDate());
			assertNull(result.getNumMeaningfulUse());
			assertNull(result.getSurveillanceCount());
			assertNull(result.getOpenNonconformityCount());
			assertNull(result.getClosedNonconformityCount());
			assertNull(result.getPracticeType());
			assertNull(result.getPreviousDevelopers());
			assertNull(result.getCriteriaMet());
			assertNull(result.getCqmsMet());
		}
	}
	

	@Transactional
	@Test
	public void testBasicSearchWithOneCustomField() throws JsonProcessingException, EntityRetrievalException {
		String resp = collectionsController.getAllCertifiedProducts("id");
		ObjectMapper mapper = new ObjectMapper();
		BasicSearchResponse results = null;
		
		try {
			results = mapper.readValue(resp, BasicSearchResponse.class);
		} catch(Exception ex) {
			fail("Caught exception " + ex.getMessage());
		}
		
		assertNotNull(results);
		assertNotNull(results.getResults());
		assertEquals(16, results.getResults().size());
		
		for(CertifiedProductFlatSearchResult result : results.getResults()) {
			assertNotNull(result.getId());
			assertNull(result.getChplProductNumber());
			assertNull(result.getEdition());
			assertNull(result.getAtl());
			assertNull(result.getAcb());
			assertNull(result.getAcbCertificationId());
			assertNull(result.getDeveloper());
			assertNull(result.getProduct());
			assertNull(result.getVersion());
			assertNull(result.getCertificationDate());
			assertNull(result.getCertificationStatus());
			assertNull(result.getDecertificationDate());
			assertNull(result.getNumMeaningfulUse());
			assertNull(result.getSurveillanceCount());
			assertNull(result.getOpenNonconformityCount());
			assertNull(result.getClosedNonconformityCount());
			assertNull(result.getPracticeType());
			assertNull(result.getPreviousDevelopers());
			assertNull(result.getCriteriaMet());
			assertNull(result.getCqmsMet());
		}
	}
	
	@Transactional
	@Test
	public void testBasicSearchWithCustomFieldsFromSubclass() throws JsonProcessingException, EntityRetrievalException {
		String resp = collectionsController.getAllCertifiedProducts("id,edition,acb,criteriaMet");
		ObjectMapper mapper = new ObjectMapper();
		BasicSearchResponse results = null;
		
		try {
			results = mapper.readValue(resp, BasicSearchResponse.class);
		} catch(Exception ex) {
			fail("Caught exception " + ex.getMessage());
		}
		
		assertNotNull(results);
		assertNotNull(results.getResults());
		assertEquals(16, results.getResults().size());
		
		for(CertifiedProductFlatSearchResult result : results.getResults()) {
			assertNotNull(result.getId());
			assertNull(result.getChplProductNumber());
			assertNotNull(result.getEdition());
			assertNull(result.getAtl());
			assertNotNull(result.getAcb());
			assertNull(result.getAcbCertificationId());
			assertNull(result.getDeveloper());
			assertNull(result.getProduct());
			assertNull(result.getVersion());
			assertNull(result.getCertificationDate());
			assertNull(result.getCertificationStatus());
			assertNull(result.getDecertificationDate());
			assertNull(result.getNumMeaningfulUse());
			assertNull(result.getSurveillanceCount());
			assertNull(result.getOpenNonconformityCount());
			assertNull(result.getClosedNonconformityCount());
			assertNull(result.getPracticeType());
			assertNull(result.getPreviousDevelopers());
			if(result.getId().longValue() == 1 || result.getId().longValue() == 2 || 
				result.getId().longValue() == 3 || result.getId().longValue() == 5 || 
				result.getId().longValue() == 10) {
				assertNotNull(result.getCriteriaMet());
			}
			assertNull(result.getCqmsMet());
		}
	}
}