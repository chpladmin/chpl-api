package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.ProductEntity;
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

	private static JWTAuthenticatedUser authUser;

	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	public void countSimpleSearchResults() {
		Long countProducts = searchResultDAO.countSimpleSearchResults("Test");
		assertNotNull(countProducts);
		assertEquals(3, countProducts.intValue());
	}
	
	@Test
	public void simpleSearch(){
		List<CertifiedProductDetailsDTO> results = searchResultDAO.simpleSearch("Test", 0, 10, "product");
		assertEquals(3, results.size());	
	}
	
	@Test
	@Transactional
	public void countMultiFilterSearchResults(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setVendor("Test");
		Long countProducts = searchResultDAO.countMultiFilterSearchResults(searchRequest);
		assertEquals(3, countProducts.intValue());
		
		searchRequest.setVersion("1.0.0");
		Long countProductsVersionSpecific = searchResultDAO.countMultiFilterSearchResults(searchRequest);
		assertEquals(1, countProductsVersionSpecific.intValue());
		
	}
	
	@Test
	@Transactional
	public void multiFilterSearchResults(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setVendor("Test");
		List<CertifiedProductDetailsDTO> products = searchResultDAO.multiFilterSearch(searchRequest, 0, 20);
		assertEquals(3, products.size());
		
		searchRequest.setVersion("1.0.0");
		List<CertifiedProductDetailsDTO> versionSpecificProducts = searchResultDAO.multiFilterSearch(searchRequest, 0, 20);
		assertEquals(1, versionSpecificProducts.size());
		
	}
	
	
	
}