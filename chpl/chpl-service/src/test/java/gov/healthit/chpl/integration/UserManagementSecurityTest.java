package gov.healthit.chpl.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.web.controller.UserManagementController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
@WebAppConfiguration
public class UserManagementSecurityTest {
		
	private MockMvc mockMvc;
	
	private static JWTAuthenticatedUser adminUser;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private Authenticator userAuthenticator;
	
	@Autowired
	private JWTUserConverter jwtUserConverter;
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private UserManagementController userManagementController;
	
	@Before
	public void setUpMockMVC(){
		//mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		//mockMvc = MockMvcBuilders.standaloneSetup(new UserManagementController()).build();
		mockMvc = MockMvcBuilders.standaloneSetup(userManagementController).build();
	}
	
	@BeforeClass
	public static void setUpAdminUser() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	
	@Test
	public void testGrantRemoveAdmin() throws Exception{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		UserDTO adminDTO = userManager.getByName("admin");
		String jwt = userAuthenticator.getJWT(adminDTO);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		
		UserDTO before = userManager.getByName("TESTUSER");
		Set<UserPermissionDTO> beforeRoles = getUserPermissions(before);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		mockMvc.perform(post("/users/grant_role").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.ALL)
				.header("Authorization", "Bearer "+jwt )
				//.header("Accept", "application/json")
				//.header("Content-Type", "application/json")
				//.contentType(MediaType.APPLICATION_JSON)
	            .content("{\"subjectName\":\"TESTUSER\",\"role\":\"ROLE_ADMIN\"}"))
	            .andDo(print())
	            .andExpect(status().is(200))
	            ;
		
		
		UserDTO after = userManager.getByName("TESTUSER");
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		Set<UserPermissionDTO> afterRoles = getUserPermissions(after);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		assertEquals(beforeRoles.size(),1);
		assertEquals(afterRoles.size(),2);
		
		String afterJwt = userAuthenticator.getJWT(after);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		// TESTUSER should be able to grant admin role, as we've given them admin privileges
		MvcResult result = mockMvc.perform(post("/users/grant_role").header("Authorization", "Bearer "+afterJwt ).contentType(MediaType.APPLICATION_JSON)
	            .content("{\"subjectName\":\"testUser2\",\"role\":\"ROLE_ADMIN\"}"))
	            .andExpect(status().is(200))
	            .andReturn();
		
		String content = result.getResponse().getContentAsString();
		
		assertTrue(content.contains("roleAdded"));
		assertTrue(content.contains("true"));
		
		UserDTO testUser2 = userManager.getByName("testUser2");
		Set<UserPermissionDTO> testUserRolesAfter = getUserPermissions(testUser2);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		assertEquals(testUserRolesAfter.size(), 2);
		
		userManager.removeAdmin("TESTUSER");
		userManager.removeAdmin("testUser2");
		
		
		UserDTO unPrivileged = userManager.getByName("TESTUSER");
		
		Set<UserPermissionDTO> unPrivilegedRoles = getUserPermissions(unPrivileged);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		assertEquals(unPrivilegedRoles.size(),1);
		assertEquals(afterRoles.size(),2);
		
		String unPrivilegedJwt = userAuthenticator.getJWT(unPrivileged);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		
		MvcResult result2 = mockMvc.perform(post("/users/grant_role").header("Authorization", "Bearer "+unPrivilegedJwt ).contentType(MediaType.APPLICATION_JSON)
	            .content("{\"subjectName\":\"testUser2\",\"role\":\"ROLE_ADMIN\"}"))
	            .andExpect(status().is(200))
	            .andReturn();
		
		String content2 = result2.getResponse().getContentAsString();
		assertTrue(content2.contains("roleAdded"));
		assertTrue(content2.contains("true"));
		
	}
	
	/*
	 * 	@Test
	public void testRemoveAdmin() throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException, JWTCreationException, JWTValidationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		UserDTO before = userManager.getByName("testUser2");
		Set<UserPermissionDTO> beforeRoles = getUserPermissions(before);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		userManager.grantAdmin("testUser2");
		
		UserDTO after = userManager.getByName("testUser2");
		
		Set<UserPermissionDTO> afterRoles = getUserPermissions(after);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		assertEquals(afterRoles.size(),2);
		assertEquals(beforeRoles.size(),1);
		
		String jwt = userAuthenticator.getJWT(after);
		User newAdmin = jwtUserConverter.getAuthenticatedUser(jwt);
		
		UserDTO testUser = userManager.getByName("TESTUSER");
		Set<UserPermissionDTO> testUserRolesBefore = getUserPermissions(testUser);
		
		SecurityContextHolder.getContext().setAuthentication(newAdmin); // set the new admin user as the current Authentication
		
		userManager.grantAdmin("TESTUSER");// If we can do this, we have Admin privileges.
		
		Set<UserPermissionDTO> testUserRolesAfter = getUserPermissions(testUser);
		SecurityContextHolder.getContext().setAuthentication(newAdmin);
		
		assertTrue(testUserRolesAfter.size() > testUserRolesBefore.size());
		
		userManager.removeAdmin("TESTUSER");
		userManager.removeAdmin("testUser2");
		
		
		UserDTO unPrivileged = userManager.getByName("testUser2");
		
		String unPrivilegedJwt = userAuthenticator.getJWT(unPrivileged);
		User nonAdmin = jwtUserConverter.getAuthenticatedUser(unPrivilegedJwt);
		
		
		SecurityContext old = SecurityContextHolder.getContext();
		
		System.out.println(old);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.getContext().setAuthentication(nonAdmin);
		
		User currentUser = Util.getCurrentUser();
		System.out.println(currentUser);
		
		userManager.grantAdmin("TESTUSER");
		Boolean grantFailed = false;
		
		// TODO: Above should be failing...
		
		try {
			userManager.grantAdmin("TESTUSER");
		} catch (Exception e){
			grantFailed = true;
		}
		SecurityContextHolder.getContext().setAuthentication(null);
		
		assertTrue(grantFailed);
		
	}
	 */
	private Set<UserPermissionDTO> getUserPermissions(UserDTO user){
		
		Authentication authenticator = new Authentication() {
			
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
				auths.add(new GrantedPermission("ROLE_USER_AUTHENTICATOR"));
				return auths;
			}

			@Override
			public Object getCredentials(){
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal(){
				return null;
			}
			@Override
			public boolean isAuthenticated(){
				return true;
			}

			@Override
			public void setAuthenticated(boolean arg0) throws IllegalArgumentException {}
			
			@Override
			public String getName(){
				return "AUTHENTICATOR";
			}
			
		};
		SecurityContextHolder.getContext().setAuthentication(authenticator);
		Set<UserPermissionDTO> permissions = userManager.getGrantedPermissionsForUser(user);
		SecurityContextHolder.getContext().setAuthentication(null);
		
		return permissions;
	}
	
}
