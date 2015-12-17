package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

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

	private static JWTAuthenticatedUser authUser;

	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	public void getAllCertifiedProducts() {
		List<CertifiedProductDetailsDTO> results = productDao.findAll();
		assertNotNull(results);
		assertEquals(4, results.size());
	}

	@Test
	public void getLargedChplNumber() {
		String largest = productDao.getLargestChplNumber();
		assertEquals("CHP-024052", largest);
	}
	
	@Test
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
	public void getProductsByVersion() {
		Long versionId = 1L;
		List<CertifiedProductDetailsDTO> products = null;
		products = productDao.getDetailsByVersionId(versionId);
		assertNotNull(products);
		assertEquals(1, products.size());
	}
}