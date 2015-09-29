package gov.healthit.chpl.auth.dao.impl;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class UserDaoTest {

	@Autowired private UserDAO dao;
	@Autowired private UserPermissionDAO permDao;
	@Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired private MutableAclService mutableAclService;
	
	private static final String ROLE_ACB_STAFF = "ROLE_ACB_STAFF";
	private static final String ROLE_ACB_ADMIN = "ROLE_ACB_ADMIN";
	private static JWTAuthenticatedUser authUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Administrator");
		authUser.setId(-2L);
		authUser.setLastName("Administrator");
		authUser.setSubjectName("admin");
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		SecurityContextHolder.getContext().setAuthentication(authUser);
	}
	
	@Test
	public void testCreateAndDeleteUser() throws UserCreationException, UserRetrievalException {
		String password = "password";
		String encryptedPassword = bCryptPasswordEncoder.encode(password);
				
		UserDTO testUser = new UserDTO();
		testUser.setAccountEnabled(true);
		testUser.setAccountExpired(false);
		testUser.setAccountLocked(false);
		testUser.setCredentialsExpired(false);
		testUser.setEmail("kekey@ainq.com");
		testUser.setFirstName("Katy");
		testUser.setLastName("Ekey-Test");
		testUser.setPhoneNumber("443-745-0987");
		testUser.setSubjectName("testUser");
		testUser.setTitle("Developer");
		testUser = dao.create(testUser, encryptedPassword);
		
		assertNotNull(testUser.getId());
		assertEquals("testUser", testUser.getSubjectName());
		
		Long insertedUserId = testUser.getId();
		dao.delete(insertedUserId);
		
		UserDTO deletedUser = dao.getById(insertedUserId);
		assertNull(deletedUser);
	}
	
	@Test
	public void testAddAcbStaffPermission() throws 
		UserRetrievalException , UserPermissionRetrievalException {
		UserDTO toEdit = dao.getByName("TESTUSER");
		assertNotNull(toEdit);
		
		dao.removePermission(toEdit.getSubjectName(), ROLE_ACB_STAFF);
		dao.addPermission(toEdit.getSubjectName(), ROLE_ACB_STAFF);
		
		Set<UserPermissionDTO> permissions = permDao.findPermissionsForUser(toEdit.getId());
		assertNotNull(permissions);
		boolean hasAcbStaffRole = false;
		for(UserPermissionDTO perm : permissions) {
			if(ROLE_ACB_STAFF.equals(perm.toString())) {
				hasAcbStaffRole = true;
			}
		}
		assertTrue(hasAcbStaffRole);
	}

	@Test
	public void testAddAcbAdminPermission() throws 
		UserRetrievalException , UserPermissionRetrievalException {
		UserDTO toEdit = dao.getByName("TESTUSER");
		assertNotNull(toEdit);
		
		dao.removePermission(toEdit.getSubjectName(), ROLE_ACB_ADMIN);
		dao.addPermission(toEdit.getSubjectName(), ROLE_ACB_ADMIN);
		
		Set<UserPermissionDTO> permissions = permDao.findPermissionsForUser(toEdit.getId());
		assertNotNull(permissions);
		boolean hasAcbStaffRole = false;
		for(UserPermissionDTO perm : permissions) {
			if(ROLE_ACB_ADMIN.equals(perm.toString())) {
				hasAcbStaffRole = true;
			}
		}
		assertTrue(hasAcbStaffRole);
	}
	
	@Test
	public void testAddInvalidPermission() throws 
		UserRetrievalException , UserPermissionRetrievalException {
		UserDTO toEdit = dao.getByName("TESTUSER");
		assertNotNull(toEdit);
		
		boolean caught = false;
		try {
			dao.addPermission(toEdit.getSubjectName(), "BOGUS");
		} catch(UserPermissionRetrievalException ex) {
			caught = true;
		}
		
		assertTrue(caught);
	}
	
	@Test
	public  void testFindUser() {
		UserDTO toFind = new UserDTO();
		toFind.setSubjectName("TESTUSER");
		toFind.setFirstName("TEST");
		toFind.setLastName("USER");
		toFind.setEmail("test@ainq.com");
		toFind.setPhoneNumber("(301) 560-6999");
		toFind.setTitle("employee");
		
		UserDTO found = dao.findUser(toFind);
		assertNotNull(found);
		assertNotNull(found.getId());
		assertEquals(1, found.getId().longValue());
		
	}
}
