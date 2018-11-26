package gov.healthit.chpl.auth.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

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
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserResetTokenDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserResetTokenDTO;
import gov.healthit.chpl.auth.entity.UserResetTokenEntity;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class UserResetTokenDaoTest {

    @Autowired
    private UserResetTokenDAO userTokenResetDao;
    private static JWTAuthenticatedUser authUser;
    private static final String RESET_PASSWORD_TOKEN = "zlhf8n4bfh87kfq";

    @BeforeClass
    public static void setUpUser() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Administrator");
        authUser.setId(-2L);
        authUser.setFriendlyName("Administrator");
        authUser.setSubjectName("admin");
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(authUser);
    }

    @Test
    @Transactional
    public void testCreatePasswordResetToken() {
        UserResetTokenDTO userResetToken = userTokenResetDao.create(RESET_PASSWORD_TOKEN, -2L);
        assertNotNull(userResetToken.getId());
        assertEquals(userResetToken.getUserResetToken(), RESET_PASSWORD_TOKEN);
    }
    
    @Test
    public void testFindByAuthToken() {
        UserResetTokenDTO found = userTokenResetDao.findByAuthToken(RESET_PASSWORD_TOKEN);
        assertNotNull(found);
        assertEquals(RESET_PASSWORD_TOKEN, found.getUserResetToken());
    }
    
    @Test
    @Transactional
    public void testdeletePreviousUserTokens() {
        userTokenResetDao.deletePreviousUserTokens(-2L);
        UserResetTokenDTO found = userTokenResetDao.findByAuthToken(RESET_PASSWORD_TOKEN);
        assertNull(found);
    }
}
