package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;


import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.VendorDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
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
		assertEquals(3, results.size());
		assertEquals(1, results.get(0).getId().longValue());
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
	public void getProductByVendor() {
		Long vendorId = 1L;
		List<ProductDTO> products = null;
		products = productDao.getByVendor(vendorId);
		assertNotNull(products);
		assertEquals(1, products.size());
	}
	
	@Test
	public void getProductByVendors() {
		List<Long> vendorIds = new ArrayList<Long>();
		vendorIds.add(1L);
		vendorIds.add(2L);
		List<ProductDTO> products = null;
		products = productDao.getByVendors(vendorIds);
		assertNotNull(products);
		assertEquals(3, products.size());
	}
}
