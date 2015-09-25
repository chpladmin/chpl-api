package gov.healthit.chpl.dao.impl;

import java.util.Date;
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
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.VendorEntity;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class VendorDaoTest extends TestCase {

	@Autowired private VendorDAO vendorDao;
	@Autowired private AddressDAO addressDao;
	
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
		assertEquals(3, results.size());
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
		Long vendorId = 3L;
		VendorDTO vendor = null;
		try {
			vendor = vendorDao.getById(vendorId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find vendor with id " + vendorId);
		}
		assertNotNull(vendor);
		assertEquals(3, vendor.getId().longValue());
		assertNull(vendor.getAddress());
	}
	
	@Test
	public void createVendorWithoutAddress() {
		VendorDTO vendor = new VendorDTO();
		vendor.setCreationDate(new Date());
		vendor.setDeleted(false);
		vendor.setLastModifiedDate(new Date());
		vendor.setLastModifiedUser(-2L);
		vendor.setName("Unit Test Vendor!");
		vendor.setWebsite("http://www.google.com");
		
		VendorDTO result = null;
		try {
			result = vendorDao.create(vendor);
		} catch(Exception ex) {
			fail("could not create vendor!");
			System.err.println(ex.getStackTrace());
		}
		
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(result.getId() > 0L);
		assertNull(vendor.getAddress());
	}
	
	@Test
	public void createVendorWithNewAddress() {
		VendorDTO vendor = new VendorDTO();
		vendor.setCreationDate(new Date());
		vendor.setDeleted(false);
		vendor.setLastModifiedDate(new Date());
		vendor.setLastModifiedUser(-2L);
		vendor.setName("Unit Test Vendor!");
		vendor.setWebsite("http://www.google.com");
		
		AddressDTO newAddress = new AddressDTO();
		newAddress.setStreetLineOne("11 Holmehurst Ave");
		newAddress.setCity("Catonsville");
		newAddress.setRegion("MD");
		newAddress.setCountry("USA");
		newAddress.setLastModifiedUser(-2L);
		newAddress.setCreationDate(new Date());
		newAddress.setLastModifiedDate(new Date());
		newAddress.setDeleted(false);
		vendor.setAddress(newAddress);
		
		VendorDTO result = null;
		try {
			result = vendorDao.create(vendor);
		} catch(Exception ex) {
			fail("could not create vendor!");
			System.err.println(ex.getStackTrace());
		}
		
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(result.getId() > 0L);
		assertNotNull(result.getAddress());
		assertNotNull(result.getAddress().getId());
		assertTrue(result.getAddress().getId() > 0L);
	}
	
	@Test
	public void createVendorWithExistingAddress() {
		VendorDTO vendor = new VendorDTO();
		vendor.setCreationDate(new Date());
		vendor.setDeleted(false);
		vendor.setLastModifiedDate(new Date());
		vendor.setLastModifiedUser(-2L);
		vendor.setName("Unit Test Vendor!");
		vendor.setWebsite("http://www.google.com");
		
		try 
		{
			AddressDTO existingAddress = addressDao.getById(1L);
			existingAddress.setCountry("Russia");
			vendor.setAddress(existingAddress);
		} catch(EntityRetrievalException ex) {
			fail("could not find existing address to set on vendor");
		}
		
		VendorDTO result = null;
		try {
			result = vendorDao.create(vendor);
		} catch(Exception ex) {
			fail("could not create vendor!");
			System.out.println(ex.getStackTrace());
		}
		
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(result.getId() > 0L);
		assertNotNull(result.getAddress());
		assertNotNull(result.getAddress().getId());
		assertTrue(result.getAddress().getId() == 1L);
		assertEquals("Russia", result.getAddress().getCountry());
	}
	
	@Test
	public void updateVendor() {
		VendorDTO vendor = vendorDao.findAll().get(0);
		vendor.setName("UPDATED NAME");
		
		VendorEntity result = null;
		try {
			result = vendorDao.update(vendor);
		} catch(Exception ex) {
			fail("could not update vendor!");
			System.out.println(ex.getStackTrace());
		}
		assertNotNull(result);

		try {
			VendorDTO updatedVendor = vendorDao.getById(vendor.getId());
			assertEquals("UPDATED NAME", updatedVendor.getName());
		} catch(Exception ex) {
			fail("could not find vendor!");
			System.out.println(ex.getStackTrace());
		}
	}
}
