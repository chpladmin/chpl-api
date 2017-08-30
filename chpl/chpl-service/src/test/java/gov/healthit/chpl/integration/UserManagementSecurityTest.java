package gov.healthit.chpl.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.filter.JWTAuthenticationFilter;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.web.controller.UserManagementController;


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
		mockMvc = MockMvcBuilders.standaloneSetup(userManagementController).addFilter(new JWTAuthenticationFilter(jwtUserConverter), "/*").build();
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
	            .content("{\"subjectName\":\"TESTUSER\",\"role\":\"ROLE_ADMIN\"}"))
	            .andDo(print())
	            .andExpect(status().is(200))
	            ;
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
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
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		String content = result.getResponse().getContentAsString();
		
		assertTrue(content.contains("roleAdded"));
		assertTrue(content.contains("true"));
		
		UserDTO testUser2 = userManager.getByName("testUser2");
		Set<UserPermissionDTO> testUserRolesAfter = getUserPermissions(testUser2);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		assertEquals(testUserRolesAfter.size(), 2);
		
		
		MvcResult result2 = mockMvc.perform(post("/users/revoke_role").header("Authorization", "Bearer "+jwt ).contentType(MediaType.APPLICATION_JSON)
	            .content("{\"subjectName\":\"testUser2\",\"role\":\"ROLE_ADMIN\"}"))
	            .andExpect(status().is(200))
	            .andReturn();
		
		String content2 = result2.getResponse().getContentAsString();
		assertTrue(content2.contains("roleRemoved"));
		assertTrue(content2.contains("true"));
		
		MvcResult result2a = mockMvc.perform(post("/users/revoke_role").header("Authorization", "Bearer "+jwt ).contentType(MediaType.APPLICATION_JSON)
	            .content("{\"subjectName\":\"TESTUSER\",\"role\":\"ROLE_ADMIN\"}"))
	            .andExpect(status().is(200))
	            .andReturn();
		
		String content2a = result2a.getResponse().getContentAsString();
		assertTrue(content2a.contains("roleRemoved"));
		assertTrue(content2a.contains("true"));
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		UserDTO unPrivileged = userManager.getByName("TESTUSER");
		
		Set<UserPermissionDTO> unPrivilegedRoles = getUserPermissions(unPrivileged);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		assertEquals(unPrivilegedRoles.size(),1);
		assertEquals(afterRoles.size(),2);
		
		String unPrivilegedJwt = userAuthenticator.getJWT(unPrivileged);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		
		Boolean grantFailed = false;
		
		try {
			
			MvcResult result3 = mockMvc.perform(post("/users/grant_role").header("Authorization", "Bearer "+unPrivilegedJwt ).contentType(MediaType.APPLICATION_JSON)
		            .content("{\"subjectName\":\"testUser2\",\"role\":\"ROLE_ADMIN\"}"))
		            .andExpect(status().is(200))
		            .andReturn();
			System.out.println(result3.getResponse().getContentAsString());
		} catch (Exception adX){
			grantFailed = true;
		}
		
		assertTrue(grantFailed);
		
	}
	
	private Set<UserPermissionDTO> getUserPermissions(UserDTO user){
		
		Authentication authenticator = new Authentication() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1222529477975713400L;

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
