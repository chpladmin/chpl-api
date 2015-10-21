package gov.healthit.chpl.auth.manager;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class UserManagerTest {
	
	@Autowired
	private UserManager userManager;
	
	
	
	private static JWTAuthenticatedUser adminUser;
	
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	@Test
	public void testCreateDeleteUser() throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, UserManagementException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		UserCreationJSONObject toCreate = new UserCreationJSONObject();
		toCreate.setEmail("email@example.com");
		toCreate.setFirstName("test");
		toCreate.setLastName("test");
		toCreate.setPassword("testpass");
		toCreate.setPhoneNumber("123-456-7890");
		toCreate.setSubjectName("testuser");
		toCreate.setTitle("Dr.");
		
		UserDTO result = userManager.create(toCreate);
		UserDTO created = userManager.getById(result.getId());
		
		assertEquals(toCreate.getEmail(), created.getEmail());
		assertEquals(toCreate.getFirstName(), created.getFirstName());
		assertEquals(toCreate.getLastName(), created.getLastName());
		assertEquals(toCreate.getSubjectName(), created.getSubjectName());
		
		userManager.delete(created);
		
		UserDTO deleted = userManager.getById(result.getId());
		assertNull(deleted);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testCreateDeleteUserByUsername() throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, UserManagementException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		UserCreationJSONObject toCreate = new UserCreationJSONObject();
		toCreate.setEmail("email@example.com");
		toCreate.setFirstName("test");
		toCreate.setLastName("test");
		toCreate.setPassword("testpass");
		toCreate.setPhoneNumber("123-456-7890");
		toCreate.setSubjectName("testuser");
		toCreate.setTitle("Dr.");
		
		UserDTO result = userManager.create(toCreate);
		UserDTO created = userManager.getById(result.getId());
		
		assertEquals(toCreate.getEmail(), created.getEmail());
		assertEquals(toCreate.getFirstName(), created.getFirstName());
		assertEquals(toCreate.getLastName(), created.getLastName());
		assertEquals(toCreate.getSubjectName(), created.getSubjectName());
		
		userManager.delete(created.getUsername());
		
		UserDTO deleted = userManager.getById(result.getId());
		assertNull(deleted);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testUpdateUser() throws UserRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		//	public UserDTO update(User userInfo) throws UserRetrievalException;
		gov.healthit.chpl.auth.json.User userInfo = new gov.healthit.chpl.auth.json.User();
		userInfo.setSubjectName("testUser2");
		userInfo.setFirstName("firstName");
		userInfo.setLastName("lastName");
		
		userManager.update(userInfo);
		
		UserDTO updated = userManager.getByName("testUser2");
		assertEquals(userInfo.getFirstName(), updated.getFirstName());
		assertEquals(userInfo.getLastName(), updated.getLastName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	public void testGetAll(){
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<UserDTO> results = userManager.getAll();
		assertEquals(results.size(), 3);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetById() throws UserRetrievalException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		UserDTO result = userManager.getById(-2L);
		assertEquals(result.getSubjectName(), "admin");
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetByName() throws UserRetrievalException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		UserDTO result = userManager.getByName("admin");
		assertEquals(-2L, (long) result.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetUserInfo() throws UserRetrievalException{
		
		UserInfoJSONObject userInfo = userManager.getUserInfo("admin");
		assertEquals(userInfo.getUser().getSubjectName(), "admin");
		assertEquals(userInfo.getUser().getFirstName(), "Administrator");
		assertEquals(userInfo.getUser().getEmail(), "info@ainq.com");
	}
	
	/*
	 * 
	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;
	public void grantAdmin(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException
	
	public void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;
	public void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;
	public void removeAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException;
	
	
	public void updateUserPassword(String userName, String password) throws UserRetrievalException;
	public String resetUserPassword(String username, String email) throws UserRetrievalException;
	
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException;


	public String encodePassword(String password);
	
	public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user);
	
	 */
	
	
}
