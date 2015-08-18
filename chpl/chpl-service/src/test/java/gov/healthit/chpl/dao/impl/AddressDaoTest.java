package gov.healthit.chpl.dao.impl;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class AddressDaoTest extends TestCase {

	@Autowired AddressDAO addressDao;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@Test
	public void getAllAddresses() {
		List<AddressDTO> results = addressDao.findAll();
		assertNotNull(results);
		assertEquals(1, results.size());
	}
	
	@Test
	public void getAddressById() {
		Long vendorId = 1L;
		AddressDTO result = null;
		try {
			result = addressDao.getById(vendorId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find address with the id");
		}
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals(1, result.getId().longValue());
	}
	
	@Test
	public void createAddress() {
		AddressDTO newAddress = new AddressDTO();
		newAddress.setStreetLineOne("800 Frederick Road");
		newAddress.setCity("Catonsville");
		newAddress.setRegion("MD");
		newAddress.setCountry("USA");
		newAddress.setLastModifiedUser(-2L);
		newAddress.setCreationDate(new Date());
		newAddress.setLastModifiedDate(new Date());
		newAddress.setDeleted(false);
		try
		{
			addressDao.create(newAddress);
		} catch(EntityRetrievalException ex) {
			fail("retrieval exception");
		} catch(EntityCreationException crex) {
			fail("creation exception");
		}
		
		assertNotNull(newAddress.getId());
		
		//try to look up the created thing
		try
		{
			AddressDTO inserted = addressDao.getById(newAddress.getId());
			assertNotNull(inserted);
		} catch(EntityRetrievalException ex) {
			fail("could not find address with id " + newAddress.getId());
		}
		
		addressDao.delete(newAddress.getId());
	}
}
