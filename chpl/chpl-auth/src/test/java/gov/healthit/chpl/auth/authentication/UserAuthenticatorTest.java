package gov.healthit.chpl.auth.authentication;

import static org.junit.Assert.*;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
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
public class UserAuthenticatorTest {
	
	@Autowired private Environment env;
	
	@Autowired
	private Authenticator authenticator;
	
	@Autowired
	private JWTUserConverter jwtUserConverter;
	
	@Test
	public void testGetDTOFromCredentials() throws BadCredentialsException, AccountStatusException, UserRetrievalException{
		
		
		LoginCredentials credentials = new LoginCredentials();
		credentials.setPassword("test");
		credentials.setUserName("TESTUSER");
		UserDTO userDTO = authenticator.getUser(credentials);
		assertNotNull(userDTO);
		
	}
	
	@Test
	public void testGetJWTFromUserDTO() throws BadCredentialsException, AccountStatusException, UserRetrievalException, JWTCreationException, JWTValidationException{
		
		LoginCredentials credentials = new LoginCredentials();
		credentials.setPassword("test");
		credentials.setUserName("TESTUSER");
		UserDTO userDTO = authenticator.getUser(credentials);
		String jwt = authenticator.getJWT(userDTO);
		assertNotNull(jwt);
		
		User user = jwtUserConverter.getAuthenticatedUser(jwt);
		assertEquals(userDTO.getSubjectName(), user.getSubjectName());
		assertEquals(userDTO.getFirstName(), user.getFirstName());
		assertEquals(userDTO.getLastName(), user.getLastName());
		
	}
	
	@Test
	public void testGetJWTFromCredentials() throws BadCredentialsException, AccountStatusException, UserRetrievalException, JWTCreationException, JWTValidationException{
		
		LoginCredentials credentials = new LoginCredentials();
		credentials.setPassword("test");
		credentials.setUserName("TESTUSER");
		String jwt = authenticator.getJWT(credentials);
		assertNotNull(jwt);
		
		User user = jwtUserConverter.getAuthenticatedUser(jwt);
		assertEquals(credentials.getUserName(), user.getSubjectName());
		
	}
	
	
	public void testRefreshJWT() throws JWTCreationException, JWTValidationException{
		
		JWTAuthenticatedUser adminUser;
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		String jwt = authenticator.refreshJWT();
		assertNotNull(jwt);
		
		User user = jwtUserConverter.getAuthenticatedUser(jwt);
		assertEquals(adminUser.getSubjectName(), user.getSubjectName());
		assertEquals(adminUser.getFirstName(), user.getFirstName());
		assertEquals(adminUser.getLastName(), user.getLastName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testLockout() {
		
		String maxAttempsEnvStr = env.getProperty("authMaximumLoginAttempts");
		int maxAttemptsEnv = Integer.parseInt(maxAttempsEnvStr);
		
		LoginCredentials badCredentials = new LoginCredentials();
		badCredentials.setPassword("badPass");
		badCredentials.setUserName("TESTUSER");
		UserDTO loggedInUser = null;
		int numAttempts = 0;
		
		while(numAttempts < maxAttemptsEnv) {
			boolean gotBadCredentialsEx = false;
			
			try {
				loggedInUser = authenticator.getUser(badCredentials);
			} catch(BadCredentialsException bc) {
				gotBadCredentialsEx = true;
			} catch(AccountStatusException as) {
				fail(as.getMessage());
			} catch(UserRetrievalException ur) {
				fail(ur.getMessage());
			}
			assertNull(loggedInUser);
			assertTrue(gotBadCredentialsEx);
			numAttempts++;
		}
		
		LoginCredentials goodCredentials = new LoginCredentials();
		goodCredentials.setPassword("test");
		goodCredentials.setUserName("TESTUSER");
		boolean gotAccountLocked = false;
		try {
			loggedInUser = authenticator.getUser(goodCredentials);
		} catch(BadCredentialsException bc) {
			fail(bc.getMessage());
		} catch(AccountStatusException as) {
			gotAccountLocked = true;
		} catch(UserRetrievalException ur) {
			fail(ur.getMessage());
		}
		assertNull(loggedInUser);
		assertTrue(gotAccountLocked);
	}
}
