package gov.healthit.chpl.dao.impl;

import java.util.Date;
import java.util.List;


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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.DeveloperEntity;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class DeveloperDaoTest extends TestCase {

	@Autowired private DeveloperDAO developerDao;
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
	public void getAllDevelopers() {
		List<DeveloperDTO> results = developerDao.findAll();
		assertNotNull(results);
		assertEquals(3, results.size());
	}

	@Test
	public void getDeveloperWithAddress() {
		Long developerId = -1L;
		DeveloperDTO developer = null;
		try {
			developer = developerDao.getById(developerId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find developer with id " + developerId);
		}
		assertNotNull(developer);
		assertEquals(-1, developer.getId().longValue());
		assertNotNull(developer.getAddress());
		assertEquals(-1, developer.getAddress().getId().longValue());
	}
	
	@Test
	public void getDeveloperWithoutAddress() {
		Long developerId = -3L;
		DeveloperDTO developer = null;
		try {
			developer = developerDao.getById(developerId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find developer with id " + developerId);
		}
		assertNotNull(developer);
		assertEquals(-3, developer.getId().longValue());
		assertNull(developer.getAddress());
	}
	
	@Test
	public void createDeveloperWithoutAddress() throws EntityRetrievalException {
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setDeleted(false);
		developer.setLastModifiedDate(new Date());
		developer.setLastModifiedUser(-2L);
		developer.setName("Unit Test Developer!");
		developer.setWebsite("http://www.google.com");
		
		DeveloperDTO result = null;
		try {
			result = developerDao.create(developer);
		} catch(Exception ex) {
			fail("could not create developer!");
			System.err.println(ex.getStackTrace());
		}
		
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(result.getId() > 0L);
		assertNull(developer.getAddress());
		assertNotNull(developerDao.getById(result.getId()));
	}
	
	@Test
	public void createDeveloperWithNewAddress() {
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setDeleted(false);
		developer.setLastModifiedDate(new Date());
		developer.setLastModifiedUser(-2L);
		developer.setName("Unit Test Developer!");
		developer.setWebsite("http://www.google.com");
		
		AddressDTO newAddress = new AddressDTO();
		newAddress.setStreetLineOne("11 Holmehurst Ave");
		newAddress.setCity("Catonsville");
		newAddress.setState("MD");
		newAddress.setZipcode("21228");
		newAddress.setCountry("USA");
		newAddress.setLastModifiedUser(-2L);
		newAddress.setCreationDate(new Date());
		newAddress.setLastModifiedDate(new Date());
		newAddress.setDeleted(false);
		developer.setAddress(newAddress);
		
		DeveloperDTO result = null;
		try {
			result = developerDao.create(developer);
		} catch(Exception ex) {
			fail("could not create developer!");
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
	public void createDeveloperWithExistingAddress() {
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setDeleted(false);
		developer.setLastModifiedDate(new Date());
		developer.setLastModifiedUser(-2L);
		developer.setName("Unit Test Developer!");
		developer.setWebsite("http://www.google.com");
		
		try 
		{
			AddressDTO existingAddress = addressDao.getById(-1L);
			existingAddress.setCountry("Russia");
			developer.setAddress(existingAddress);
		} catch(EntityRetrievalException ex) {
			fail("could not find existing address to set on developer");
		}
		
		DeveloperDTO result = null;
		try {
			result = developerDao.create(developer);
		} catch(Exception ex) {
			fail("could not create developer!");
			System.out.println(ex.getStackTrace());
		}
		
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(result.getId() > 0L);
		assertNotNull(result.getAddress());
		assertNotNull(result.getAddress().getId());
		assertTrue(result.getAddress().getId() == -1L);
		assertEquals("Russia", result.getAddress().getCountry());
	}
	
	@Test
	public void updateDeveloper() {
		DeveloperDTO developer = developerDao.findAll().get(0);
		developer.setName("UPDATED NAME");
		
		DeveloperEntity result = null;
		try {
			result = developerDao.update(developer);
		} catch(Exception ex) {
			fail("could not update developer!");
			System.out.println(ex.getStackTrace());
		}
		assertNotNull(result);

		try {
			DeveloperDTO updatedDeveloper = developerDao.getById(developer.getId());
			assertEquals("UPDATED NAME", updatedDeveloper.getName());
		} catch(Exception ex) {
			fail("could not find developer!");
			System.out.println(ex.getStackTrace());
		}
	}
	
	@Test
	@Transactional
	public void createDeveloperAcbMap() {
		SecurityContextHolder.getContext().setAuthentication(authUser);
		DeveloperDTO developer = developerDao.findAll().get(0);
		
		DeveloperACBMapDTO dto = new DeveloperACBMapDTO();
		dto.setAcbId(-3L);
		dto.setDeveloperId(developer.getId());
		dto.setTransparencyAttestation("N/A");
		DeveloperACBMapDTO createdMapping = developerDao.createTransparencyMapping(dto);
		
		assertNotNull(createdMapping);
		
		dto = developerDao.getTransparencyMapping(developer.getId(), -3L);
		assertNotNull(dto);
		assertEquals("N/A", dto.getTransparencyAttestation());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
