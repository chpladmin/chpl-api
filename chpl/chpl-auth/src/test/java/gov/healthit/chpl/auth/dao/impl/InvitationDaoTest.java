package gov.healthit.chpl.auth.dao.impl;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.MutableAclService;
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

import gov.healthit.chpl.auth.dao.InvitationDAO;
import gov.healthit.chpl.auth.dao.InvitationPermissionDAO;
import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.InvitationPermissionDTO;
import gov.healthit.chpl.auth.permission.GrantedPermission;
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
public class InvitationDaoTest {

	@Autowired private InvitationDAO dao;
	@Autowired private InvitationPermissionDAO permDao;
	@Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired private MutableAclService mutableAclService;
	
	private static final String ROLE_CHPL_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_ACB = "ROLE_ACB";
	private static final String ROLE_ONC_STAFF = "ROLE_ONC_STAFF";
	private static JWTAuthenticatedUser chplAdminUser;
	private static JWTAuthenticatedUser acbAdminUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		chplAdminUser = new JWTAuthenticatedUser();
		chplAdminUser.setFullName("Administrator");
		chplAdminUser.setId(-2L);
		chplAdminUser.setFriendlyName("Administrator");
		chplAdminUser.setSubjectName("admin");
		chplAdminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	@Test
	@Transactional
	public void testInviteChplAdmin() throws UserCreationException, UserRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(chplAdminUser);

		String emailAddress = "katy.ekey@gmail.com";
		String token = bCryptPasswordEncoder.encode(emailAddress);
				
		InvitationDTO testDto = new InvitationDTO();
		testDto.setCreationDate(new Date());
		testDto.setDeleted(false);
		testDto.setEmail(emailAddress);
		testDto.setInviteToken(token);
		InvitationPermissionDTO permissionDto = new InvitationPermissionDTO();
		permissionDto.setPermissionName(ROLE_CHPL_ADMIN);
		permissionDto.setPermissionId(-2L);

		testDto = dao.create(testDto);
		
		assertNotNull(testDto.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional
	public void testInviteOncStaff() throws UserCreationException, UserRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(chplAdminUser);

		String emailAddress = "dlucas@ainq.com";
		String token = bCryptPasswordEncoder.encode(emailAddress);
				
		InvitationDTO testDto = new InvitationDTO();
		testDto.setCreationDate(new Date());
		testDto.setDeleted(false);
		testDto.setEmail(emailAddress);
		testDto.setInviteToken(token);
		InvitationPermissionDTO permissionDto = new InvitationPermissionDTO();
		permissionDto.setPermissionName(ROLE_ONC_STAFF);
		permissionDto.setPermissionId(-2L);

		testDto = dao.create(testDto);
		
		assertNotNull(testDto.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
