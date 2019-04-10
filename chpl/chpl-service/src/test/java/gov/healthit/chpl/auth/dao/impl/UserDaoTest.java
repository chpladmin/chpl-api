package gov.healthit.chpl.auth.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class UserDaoTest {

    @Autowired
    private UserDAO dao;
    @Autowired
    private UserPermissionDAO permDao;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static final String ROLE_ACB = "ROLE_ACB";
    private static JWTAuthenticatedUser authUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Administrator");
        authUser.setId(-2L);
        authUser.setFriendlyName("Administrator");
        authUser.setSubjectName("admin");
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test(expected = UserRetrievalException.class)
    @Transactional
    @Rollback
    public void testCreateAndDeleteUser()
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        String password = "password";
        String encryptedPassword = bCryptPasswordEncoder.encode(password);

        UserDTO testUser = new UserDTO();
        testUser.setAccountEnabled(true);
        testUser.setAccountExpired(false);
        testUser.setAccountLocked(false);
        testUser.setCredentialsExpired(false);
        testUser.setEmail("kekey@ainq.com");
        testUser.setFullName("Katy");
        testUser.setFriendlyName("Ekey-Test");
        testUser.setPhoneNumber("443-745-0987");
        testUser.setSubjectName("testUser");
        testUser.setTitle("Developer");
        testUser.setPermission(permDao.getPermissionFromAuthority(Authority.ROLE_CMS_STAFF));
        testUser = dao.create(testUser, encryptedPassword);

        assertNotNull(testUser.getId());
        assertNotNull(testUser.getPermission());
        assertEquals(Authority.ROLE_CMS_STAFF, testUser.getPermission().getAuthority());
        assertEquals("testUser", testUser.getSubjectName());

        Long insertedUserId = testUser.getId();
        dao.delete(insertedUserId);

        dao.getById(insertedUserId);
    }

    @Test
    public void testFindUser() {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        UserDTO toFind = new UserDTO();
        toFind.setSubjectName("TESTUSER");
        toFind.setFullName("TEST");
        toFind.setFriendlyName("USER");
        toFind.setEmail("test@ainq.com");
        toFind.setPhoneNumber("(301) 560-6999");
        toFind.setTitle("employee");

        UserDTO found = dao.findUser(toFind);
        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals(1, found.getId().longValue());

    }

    /**
     * Given the DAO is called When the passed in user id has been deleted Then
     * null is returned
     * 
     * @throws UserRetrievalException
     */
    @Test(expected = UserRetrievalException.class)
    public void testGetById_returnsNullForDeletedUser() throws UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        dao.getById(-3L);
    }

    /**
     * Given the DAO is called When the passed in user id is valid/active Then a
     * result is returned
     * 
     * @throws UserRetrievalException
     */
    @Test
    public void testGetById_returnsResultForActiveUser() throws UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        UserDTO userDto = null;
        userDto = dao.getById(-2L);
        assertTrue(userDto != null);
    }
}
