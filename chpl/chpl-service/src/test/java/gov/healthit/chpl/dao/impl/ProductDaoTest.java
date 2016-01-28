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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
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
public class ProductDaoTest extends TestCase {

	@Autowired
	private ProductDAO productDao;

	private static JWTAuthenticatedUser authUser;

	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	public void getAllProducts() {
		List<ProductDTO> results = productDao.findAll();
		assertNotNull(results);
		assertEquals(4, results.size());
	}

	@Test
	public void getProductById() {
		Long productId = 1L;
		ProductDTO product = null;
		try {
			product = productDao.getById(productId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find product with id " + productId);
		}
		assertNotNull(product);
		assertEquals(1, product.getId().longValue());
	}
	
	@Test
	public void getProductByDeveloper() {
		Long developerId = -1L;
		List<ProductDTO> products = null;
		products = productDao.getByDeveloper(developerId);
		assertNotNull(products);
		assertEquals(3, products.size());
	}
	
	@Test
	public void getProductByDevelopers() {
		List<Long> developerIds = new ArrayList<Long>();
		developerIds.add(-1L);
		developerIds.add(-2L);
		List<ProductDTO> products = null;
		products = productDao.getByDevelopers(developerIds);
		assertNotNull(products);
		assertEquals(4, products.size());
	}
	
	@Test
	public void updateProduct() throws EntityRetrievalException {
		ProductDTO product = productDao.getById(1L);
		product.setDeveloperId(-2L);
		
		ProductEntity result = null;
		try {
			result = productDao.update(product);
		} catch(Exception ex) {
			fail("could not update product!");
			System.out.println(ex.getStackTrace());
		}
		assertNotNull(result);

		try {
			ProductDTO updatedProduct = productDao.getById(product.getId());
			assertTrue(updatedProduct.getDeveloperId() == -2L);
		} catch(Exception ex) {
			fail("could not find product!");
			System.out.println(ex.getStackTrace());
		}
	}
}