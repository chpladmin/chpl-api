package old.gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.web.controller.AnnouncementController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class AnnouncementControllerTest {
    private static JWTAuthenticatedUser adminUser;
    private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

    @Autowired
    Environment env;

    @Autowired
    AnnouncementController announcementController;

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
    public void testGetAnnouncementByBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        announcementController.getAnnouncementById(-100L);
    }

    @Transactional
    @Test
    public void testCreatePublicAnnouncement()
            throws JsonProcessingException, EntityCreationException,
            EntityRetrievalException, UserRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Announcement toCreate = new Announcement();
        toCreate.setEndDate(new Date(System.currentTimeMillis() + ONE_DAY_MILLIS));
        toCreate.setStartDate(new Date());
        toCreate.setIsPublic(Boolean.TRUE);
        toCreate.setText("Test");
        toCreate.setTitle("Test");
        Announcement result = announcementController.create(toCreate);

        assertEquals(true, result.getIsPublic().booleanValue());
        assertEquals(toCreate.getTitle(), result.getTitle());
        assertEquals(toCreate.getText(), result.getText());
        assertEquals(toCreate.getStartDate().getTime(), result.getStartDate().getTime());
        assertEquals(toCreate.getEndDate().getTime(), result.getEndDate().getTime());
    }

    @Transactional
    @Test
    public void testCreatePrivateAnnouncement()
            throws JsonProcessingException, EntityCreationException,
            EntityRetrievalException, UserRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Announcement toCreate = new Announcement();
        toCreate.setEndDate(new Date(System.currentTimeMillis() + ONE_DAY_MILLIS));
        toCreate.setStartDate(new Date());
        toCreate.setIsPublic(Boolean.FALSE);
        toCreate.setText("Test");
        toCreate.setTitle("Test");
        Announcement result = announcementController.create(toCreate);

        assertEquals(false, result.getIsPublic().booleanValue());
        assertEquals(toCreate.getTitle(), result.getTitle());
        assertEquals(toCreate.getText(), result.getText());
        assertEquals(toCreate.getStartDate().getTime(), result.getStartDate().getTime());
        assertEquals(toCreate.getEndDate().getTime(), result.getEndDate().getTime());
    }
}
