package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.constraints.AssertFalse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = gov.healthit.chpl.CHPLTestConfig.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductDaoTest {

	@Autowired
	private CertifiedProductDAO productDao;
	static final String URL_PATTERN = "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";

	private static JWTAuthenticatedUser authUser;
	private static Pattern urlPattern = Pattern.compile(URL_PATTERN);
	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	public void testUrlPattern() {
		String badUrl = "www.google.com";
		
		assertEquals(false, urlPattern.matcher(badUrl).matches());
		
		String goodUrl = "http://www.google.com";
		assertEquals(true, urlPattern.matcher(goodUrl).matches());
	}
	
	@Test
	@Transactional(readOnly = true)
	public void getAllCertifiedProducts() {
		List<CertifiedProductDetailsDTO> results = productDao.findAll();
		assertNotNull(results);
		assertEquals(6, results.size());
	}
	
	@Test
	@Transactional(readOnly = true)
	public void getById() {
		Long productId = 1L;
		CertifiedProductDTO product = null;
		try {
			product = productDao.getById(productId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find version with id " + productId);
		}
		assertNotNull(product);
		assertEquals(1, product.getId().longValue());
	}
	
	@Test
	@Transactional(readOnly = true)
	public void getProductsByVersion() {
		Long versionId = 1L;
		List<CertifiedProductDetailsDTO> products = null;
		products = productDao.getDetailsByVersionId(versionId);
		assertNotNull(products);
		assertEquals(2, products.size());
	}
	
	@Test
	@Transactional(readOnly = true)
	public void getDetailsByChplNumbers() {
		List<String> chplProductNumbers = new ArrayList<String>();
		chplProductNumbers.add("CHP-024050");
		chplProductNumbers.add("CHP-024051");
		chplProductNumbers.add("CHP-024052");
		List<CertifiedProductDetailsDTO> products = null;
		products = productDao.getDetailsByChplNumbers(chplProductNumbers);
		assertNotNull(products);
		assertEquals(3, products.size());
	}
	
	/**
	 * Given that I am ROLE_ONC_STAFF or ROLE_ADMIN
	 * When I update a CHPL Product Number's count of meaningfulUseUsers
	 * Then the database shows the change for only the CHPL Product Number's meaningfulUseUsers
	 * @throws EntityRetrievalException
	 * @throws IOException 
	 */
	@Test
	@Transactional(readOnly = true)
	public void updateMeaningfulUseUsers() throws EntityRetrievalException, IOException {
		SecurityContextHolder.getContext().setAuthentication(authUser);
		CertifiedProductDTO dto = new CertifiedProductDTO();
		dto.setChplProductNumber("CHP-024050");
		dto.setMeaningfulUseUsers(11L);
		CertifiedProductDTO dtoResponse = new CertifiedProductDTO();
		dtoResponse = productDao.updateMeaningfulUseUsers(dto);
		assertNotNull(dtoResponse);
		assertTrue(dtoResponse.getChplProductNumber().equalsIgnoreCase("CHP-024050"));
		assertTrue(dtoResponse.getMeaningfulUseUsers() == 11L);
		assertTrue(dtoResponse.getCertificationEditionId() != null);
	}
	
	/**
	 * Given that I am authenticated as an admin
	 * When I delete a certified product
	 * Then that certified product no longer exists in the database
	 * @throws EntityRetrievalException 
	 */
	@Test
	@Transactional(readOnly = false)
	public void delete() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(authUser);
		Long productId = 1L;
		productDao.delete(productId);
		CertifiedProductDTO deletedProduct = productDao.getById(productId);
		assertTrue(deletedProduct.getDeleted());
	}
	
//	@Test
//	public void getByUniqueId() throws EntityRetrievalException {
//		String id = "14.";
//		CertifiedProductDetailsDTO cpDetails = productDao.getByChplUniqueId(id);
//		assertNotNull(cpDetails);
//		assertEquals(cpDetails.getChplProductNumber(), id);
//	}
}