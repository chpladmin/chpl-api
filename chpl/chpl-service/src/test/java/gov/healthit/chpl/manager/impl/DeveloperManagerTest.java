package gov.healthit.chpl.manager.impl;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.entity.DeveloperStatusType;
import gov.healthit.chpl.manager.DeveloperManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class DeveloperManagerTest extends TestCase {
	
	@Autowired private DeveloperManager developerManager;
	@Autowired private DeveloperStatusDAO devStatusDao;
	
	private static JWTAuthenticatedUser adminUser;
	private static JWTAuthenticatedUser testUser3;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		
		testUser3 = new JWTAuthenticatedUser();
		testUser3.setFirstName("Test");
		testUser3.setId(3L);
		testUser3.setLastName("User3");
		testUser3.setSubjectName("testUser3");
		testUser3.getPermissions().add(new GrantedPermission("ROLE_ACB_ADMIN"));
	}
	
	@Test
	public void testGetDeveloperAsAdmin() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		DeveloperDTO developer = developerManager.getById(-1L);
		assertNotNull(developer);
		assertNotNull(developer.getTransparencyAttestationMappings());
		assertTrue(developer.getTransparencyAttestationMappings().size() > 0);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetDeveloperAsAcbAdmin() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		DeveloperDTO developer = developerManager.getById(-1L);
		assertNotNull(developer);
		assertNotNull(developer.getTransparencyAttestationMappings());
		for(DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
			if(attMap.getDeveloperId().longValue() == developer.getId().longValue()) {
				assertEquals("Affirmative", attMap.getTransparencyAttestation());
			}
		}
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Rollback
	public void testDeveloperStatusChangeAllowedByAdmin() 
			throws EntityRetrievalException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		DeveloperDTO developer = developerManager.getById(-1L);
		assertNotNull(developer);
		DeveloperStatusDTO newStatus = devStatusDao.getById(2L);
		developer.setStatus(newStatus);
		
		boolean failed = false;
		try {
			developer = developerManager.update(developer);
		} catch(EntityCreationException ex) {
			System.out.println(ex.getMessage());
			failed = true;
		}
		assertFalse(failed);
		assertNotNull(developer.getStatus());
		assertEquals(DeveloperStatusType.SuspendedByOnc.toString(), developer.getStatus().getStatusName());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Rollback
	public void testNoUpdatesAllowedByNonAdminIfDeveloperIsNotActive() 
			throws EntityRetrievalException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		DeveloperDTO developer = developerManager.getById(-3L);
		assertNotNull(developer);
		assertNotNull(developer.getStatus());
		assertNotSame(DeveloperStatusType.Active.toString(), developer.getStatus().getStatusName());
		
		developer.setName("UPDATE THIS NAME");
		boolean failed = false;
		try {
			developer = developerManager.update(developer);
		} catch(EntityCreationException ex) {
			System.out.println(ex.getMessage());
			failed = true;
		}
		assertTrue(failed);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Rollback
	public void testDeveloperStatusChangeNotAllowedByNonAdmin() 
			throws EntityRetrievalException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		DeveloperDTO developer = developerManager.getById(-1L);
		assertNotNull(developer);
		DeveloperStatusDTO newStatus = devStatusDao.getById(2L);
		developer.setStatus(newStatus);
		
		boolean failed = false;
		try {
			developerManager.update(developer);
		} catch(EntityCreationException ex) {
			System.out.println(ex.getMessage());
			failed = true;
		}
		assertTrue(failed);
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
