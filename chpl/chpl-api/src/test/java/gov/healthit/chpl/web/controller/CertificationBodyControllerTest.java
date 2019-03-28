package gov.healthit.chpl.web.controller;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
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

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationBodyControllerTest {
    private static JWTAuthenticatedUser adminUser;

    @Autowired
    Environment env;

    @Autowired
    CertificationBodyController acbController;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(UnitTestUtil.ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetAcbByBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        acbController.getAcbById(-100L);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testRemoveUserFromAcbByBadAcbId() throws EntityRetrievalException, IOException,
            InvalidArgumentsException, ValidationException, EntityCreationException, UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        acbController.deleteUserFromAcb(-100L, -2L);
    }

    @Transactional
    @Test(expected = UserRetrievalException.class)
    public void testRemoveUserFromAcbByBadUserId() throws EntityRetrievalException, IOException,
            InvalidArgumentsException, ValidationException, EntityCreationException, UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        acbController.deleteUserFromAcb(-1L, -100L);
    }
}
