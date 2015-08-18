package gov.healthit.chpl.dao.impl;

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
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.VendorDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class VendorDaoTest extends TestCase {

	@Autowired
	private VendorDAO vendorDao;

	private static JWTAuthenticatedUser authUser;

	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	public void getAllVendors() {
		List<VendorDTO> results = vendorDao.findAll();
		assertNotNull(results);
		assertEquals(2, results.size());
	}

	@Test
	public void getVendorWithAddress() {
		Long vendorId = 1L;
		VendorDTO vendor = null;
		try {
			vendor = vendorDao.getById(vendorId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find vendor with id " + vendorId);
		}
		assertNotNull(vendor);
		assertEquals(1, vendor.getId().longValue());
		assertNotNull(vendor.getAddress());
		assertEquals(1, vendor.getAddress().getId().longValue());
	}
	
	@Test
	public void getVendorWithoutAddress() {
		Long vendorId = 2L;
		VendorDTO vendor = null;
		try {
			vendor = vendorDao.getById(vendorId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find vendor with id " + vendorId);
		}
		assertNotNull(vendor);
		assertEquals(2, vendor.getId().longValue());
		assertNull(vendor.getAddress());
	}
}
