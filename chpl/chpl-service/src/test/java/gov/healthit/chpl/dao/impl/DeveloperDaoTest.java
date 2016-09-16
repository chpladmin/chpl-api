package gov.healthit.chpl.dao.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
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

	@Autowired 
	private DeveloperDAO developerDao;
	
	@Autowired 
	private AddressDAO addressDao;
	
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
	
	/** Description: Tests the getByCreationDate(startDate, endDate) method
	 * Verifies that results are returned
	 * Verifies that results are after the startDate and before the endDate
	 * Expected Result: One or more results
	 * All results have a creationDate after the startDate and before the endDate
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 * @throws ParseException 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getByCreationDate_ReturnsValidDevelopers() throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ParseException{
		SecurityContextHolder.getContext().setAuthentication(authUser);
		
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		Date startDate = isoFormat.parse("2016-01-01");
		Date endDate = isoFormat.parse("2016-09-16");
		
		List<DeveloperDTO> result = developerDao.getByCreationDate(startDate, endDate);
		
		assertTrue("getByCreationDate() should return valid DeveloperDTO results but returned " + result.size(), result.size() > 0);
		for(DeveloperDTO dto : result){
			assertTrue("startDate of " + startDate + " should be before " + dto.getCreationDate(), startDate.before(dto.getCreationDate()));
			assertTrue("endDate of " + endDate + " should be after " + dto.getCreationDate(), endDate.after(dto.getCreationDate()));
		}

	}
}
