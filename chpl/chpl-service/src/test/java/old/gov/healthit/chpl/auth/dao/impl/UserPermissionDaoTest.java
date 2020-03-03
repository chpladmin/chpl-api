package old.gov.healthit.chpl.auth.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class UserPermissionDaoTest {

    @Autowired
    private UserPermissionDAO permDao;

    private static JWTAuthenticatedUser authUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Administrator");
        authUser.setId(-2L);
        authUser.setFriendlyName("Administrator");
        authUser.setSubjectName("admin");
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(authUser);
    }

    @Test
    public void testAdminPermissionExists() throws UserPermissionRetrievalException {
        testPermissionExists(Authority.ROLE_ADMIN);
    }

    @Test
    public void testOncPermissionExists() throws UserPermissionRetrievalException {
        testPermissionExists(Authority.ROLE_ONC);
    }

    @Test
    public void testAcbPermissionExists() throws UserPermissionRetrievalException {
        testPermissionExists(Authority.ROLE_ACB);
    }

    @Test
    public void testAtlPermissionExists() throws UserPermissionRetrievalException {
        testPermissionExists(Authority.ROLE_ATL);
    }

    @Test
    public void testCmsPermissionExists() throws UserPermissionRetrievalException {
        testPermissionExists(Authority.ROLE_CMS_STAFF);
    }

    @Test (expected = UserPermissionRetrievalException.class)
    public void testBogusPermissionNotExists() throws UserPermissionRetrievalException {
        testPermissionExists("BOGUS");
    }

    private void testPermissionExists(final String authority) throws UserPermissionRetrievalException {
        UserPermissionDTO permission = permDao.getPermissionFromAuthority(authority);
        assertNotNull(permission);
        assertEquals(authority, permission.getAuthority());
    }
}
