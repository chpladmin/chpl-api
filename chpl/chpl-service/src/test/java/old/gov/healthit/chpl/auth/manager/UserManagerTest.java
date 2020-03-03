package old.gov.healthit.chpl.auth.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.UserManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class UserManagerTest {

    @Autowired
    private UserPermissionDAO permDao;

    @Autowired
    private UserManager userManager;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser, oncUser, acbUser, atlUser, cmsUser;

    private static final String RESET_PASSWORD_TOKEN = "zlhf8n4bfh87kfq";

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        oncUser = new JWTAuthenticatedUser();
        oncUser.setFullName("ONC User");
        oncUser.setId(6L);
        oncUser.setFriendlyName("Onc");
        oncUser.setSubjectName("oncUser");
        oncUser.getPermissions().add(new GrantedPermission("ROLE_ONC"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(4L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));

        cmsUser = new JWTAuthenticatedUser();
        cmsUser.setFullName("CMS User");
        cmsUser.setId(5L);
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsStaffUser");
        cmsUser.getPermissions().add(new GrantedPermission("ROLE_CMS_STAFF"));
    }

    @Test(expected = UserRetrievalException.class)
    @Transactional
    @Rollback(true)
    public void testCreateDeleteUser() throws UserCreationException, UserRetrievalException,
            UserPermissionRetrievalException, UserManagementException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        String password = "a long password that should be good enough to not be blocked";
        UserDTO toCreate = new UserDTO();
        toCreate.setEmail("email@example.com");
        toCreate.setFullName("test");
        toCreate.setFriendlyName("test");
        toCreate.setPhoneNumber("123-456-7890");
        toCreate.setSubjectName("testuser");
        toCreate.setTitle("Dr.");
        toCreate.setPermission(permDao.getPermissionFromAuthority(Authority.ROLE_ADMIN));

        UserDTO result = userManager.create(toCreate, password);
        UserDTO created = userManager.getById(result.getId());

        assertEquals(toCreate.getEmail(), created.getEmail());
        assertEquals(toCreate.getFullName(), created.getFullName());
        assertEquals(toCreate.getFriendlyName(), created.getFriendlyName());
        assertEquals(toCreate.getSubjectName(), created.getSubjectName());

        userManager.delete(created);
        userManager.getById(result.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = UserRetrievalException.class)
    @Transactional
    @Rollback(true)
    public void testCreateDeleteUserByUsername() throws UserCreationException, UserRetrievalException,
            UserPermissionRetrievalException, UserManagementException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String password = "a long password that should be good enough to not be blocked";
        UserDTO toCreate = new UserDTO();
        toCreate.setEmail("email@example.com");
        toCreate.setFullName("test");
        toCreate.setFriendlyName("test");
        toCreate.setPhoneNumber("123-456-7890");
        toCreate.setSubjectName("testuser");
        toCreate.setTitle("Dr.");
        toCreate.setPermission(permDao.getPermissionFromAuthority(Authority.ROLE_ADMIN));

        UserDTO result = userManager.create(toCreate, password);
        UserDTO created = userManager.getById(result.getId());

        assertEquals(toCreate.getEmail(), created.getEmail());
        assertEquals(toCreate.getFullName(), created.getFullName());
        assertEquals(toCreate.getFriendlyName(), created.getFriendlyName());
        assertEquals(toCreate.getSubjectName(), created.getSubjectName());

        userManager.delete(created.getUsername());
        userManager.getById(result.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Verify that a weak password results in a Create User exception.
     * 
     * @throws UserCreationException
     *             expected
     * @throws UserRetrievalException
     *             not expected
     * @throws UserPermissionRetrievalException
     *             not expected
     * @throws UserManagementException
     *             not expected
     */
    @Test(expected = UserCreationException.class)
    @Transactional
    @Rollback(true)
    public void testCreateDeleteUserWithBadPassword() throws UserCreationException, UserRetrievalException,
            UserPermissionRetrievalException, UserManagementException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        String password = "weak";
        UserDTO toCreate = new UserDTO();
        toCreate.setEmail("email@example.com");
        toCreate.setFullName("test");
        toCreate.setFriendlyName("test");
        toCreate.setPhoneNumber("123-456-7890");
        toCreate.setSubjectName("testuser");
        toCreate.setTitle("Dr.");

        UserDTO result = userManager.create(toCreate, password);
        UserDTO created = userManager.getById(result.getId());

        assertEquals(toCreate.getEmail(), created.getEmail());
        assertEquals(toCreate.getFullName(), created.getFullName());
        assertEquals(toCreate.getFriendlyName(), created.getFriendlyName());
        assertEquals(toCreate.getSubjectName(), created.getSubjectName());

        userManager.delete(created.getUsername());
        userManager.getById(result.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testUpdateUser() throws UserRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        UserDTO userInfo = new UserDTO();
        userInfo.setSubjectName("testUser2");
        userInfo.setFullName("firstName");
        userInfo.setFriendlyName("lastName");
        userInfo.setEmail("test@test.com");
        userInfo.setPhoneNumber("111-111-1111");

        userManager.update(userInfo);

        UserDTO updated = userManager.getByName("testUser2");
        assertEquals(userInfo.getFullName(), updated.getFullName());
        assertEquals(userInfo.getFriendlyName(), updated.getFriendlyName());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetAllAsAdminUser() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<UserDTO> results = userManager.getAll();
        assertEquals(8, results.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetAllAsOncUser() {
        SecurityContextHolder.getContext().setAuthentication(oncUser);
        List<UserDTO> results = userManager.getAll();
        assertEquals(8, results.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetAllAsAcbUser() {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<UserDTO> results = userManager.getAll();
        assertEquals(2, results.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetAllAsAtlUser() {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        List<UserDTO> results = userManager.getAll();
        assertEquals(2, results.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetAllAsCmsUser() {
        SecurityContextHolder.getContext().setAuthentication(cmsUser);
        List<UserDTO> results = userManager.getAll();
        assertEquals(1, results.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetById() throws UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        UserDTO result = userManager.getById(-2L);
        assertEquals(result.getSubjectName(), "admin");
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = UserRetrievalException.class)
    @Transactional
    @Rollback(true)
    public void testGetByIdNotFound() throws UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        userManager.getById(-6000L);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetByName() throws UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        UserDTO result = userManager.getByName("admin");
        assertEquals(-2L, (long) result.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetUserInfo() throws UserRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        gov.healthit.chpl.domain.auth.User userInfo = userManager.getUserInfo("admin");
        assertEquals(userInfo.getSubjectName(), "admin");
        assertEquals(userInfo.getFullName(), "Administrator");
        assertEquals(userInfo.getEmail(), "info@ainq.com");

    }

    @Test
    public void testPasswordResetValid() throws UserRetrievalException {
        UserResetTokenDTO tokenDTO = userManager.createResetUserPasswordToken("admin", "info@ainq.com");
        assertNotNull(tokenDTO);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        Date oneDayAgo = calendar.getTime();
        tokenDTO.setCreationDate(oneDayAgo);
        assertTrue(userManager.authorizePasswordReset(tokenDTO.getUserResetToken()));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testPasswordResetInvalid() throws UserRetrievalException {
        assertFalse(userManager.authorizePasswordReset(RESET_PASSWORD_TOKEN));
    }

}
