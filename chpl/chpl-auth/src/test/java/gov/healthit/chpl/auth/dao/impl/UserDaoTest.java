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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional
	public void testAddAclPermission() throws UserRetrievalException, UserCreationException {
		String password = "password";
		String encryptedPassword = bCryptPasswordEncoder.encode(password);
		
		UserDTO testUser = dao.getByName("kekey3");
		
		if(testUser == null) {
			testUser = new UserDTO();
			testUser.setAccountEnabled(true);
			testUser.setAccountExpired(false);
			testUser.setAccountLocked(false);
			testUser.setCredentialsExpired(false);
			testUser.setEmail("kekey@ainq.com");
			testUser.setFirstName("Katy");
			testUser.setLastName("Ekey-Test");
			testUser.setPhoneNumber("443-745-0987");
			testUser.setSubjectName("kekey3");
			testUser.setTitle("Developer");
			testUser = dao.create(testUser, encryptedPassword);
		}
		
		assertNotNull(testUser.getId());
		assertEquals("kekey3", testUser.getSubjectName());
		assertTrue(testUser.getId().longValue() > 0);
		
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, testUser.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}
		
		acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, 
				new PrincipalSid(testUser.getSubjectName()), true);
		mutableAclService.updateAcl(acl);
	}
	
	@Test
	public void testAddAcbStaffPermission() throws 
		UserRetrievalException , UserPermissionRetrievalException {
		UserDTO toEdit = dao.getByName("kekey3");
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
		UserDTO toEdit = dao.getByName("kekey3");
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
		UserDTO toEdit = dao.getByName("kekey3");
		assertNotNull(toEdit);
		
		boolean caught = false;
		try {
			dao.addPermission(toEdit.getSubjectName(), "BOGUS");
		} catch(UserPermissionRetrievalException ex) {
			caught = true;
		}
		
		assertTrue(caught);
	}
}
