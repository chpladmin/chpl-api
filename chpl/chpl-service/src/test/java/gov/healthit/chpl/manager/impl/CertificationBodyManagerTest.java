package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationBodyManagerTest extends TestCase {
	
	@Autowired private CertificationBodyManager acbManager;
	@Autowired private CertificationBodyDAO acbDao;
	@Autowired private UserDAO userDao;
	
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
	public void testGetUsersOnAcb() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationBodyDTO acb = acbDao.getById(-3L);
		List<UserDTO> users = acbManager.getAllUsersOnAcb(acb);
		
		assertEquals(2, users.size());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testAddReadUserToAcb() throws UserRetrievalException, EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		//add to the acb
		CertificationBodyDTO acb = acbDao.getById(-3L);
		Long userId = 2L;
		acbManager.addPermission(acb, userId, BasePermission.READ);
		
		//confirm one user is in the acb
		List<UserDTO> users = acbManager.getAllUsersOnAcb(acb);
		boolean userIsOnAcb = false;
		for(UserDTO foundUser : users) {
			if(foundUser.getId() == userId) {
				userIsOnAcb = true;
			}
		}
		assertTrue(userIsOnAcb);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testDeleteUserFromAcb() throws UserRetrievalException, EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		//add to the acb
		CertificationBodyDTO acb = acbDao.getById(-3L);
		UserDTO user = userDao.getById(1L);
		acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), BasePermission.ADMINISTRATION);
		
		//confirm one user is in the acb
		List<UserDTO> users = acbManager.getAllUsersOnAcb(acb);
		boolean userIsOnAcb = false;
		for(UserDTO foundUser : users) {
			if(foundUser.getId() == user.getId()) {
				userIsOnAcb = true;
			}
		}
		assertFalse(userIsOnAcb);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
