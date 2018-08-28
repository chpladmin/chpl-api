package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.NotificationManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class NotificationManagerTest extends TestCase {

    @Autowired private NotificationManager notificationManager;
    @Autowired private NotificationDAO notificationDao;
    @Autowired private CertificationBodyDAO acbDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;
    private static JWTAuthenticatedUser acbUser;
    private static JWTAuthenticatedUser acbUser2;
    private static JWTAuthenticatedUser atlUser;
    private static List<NotificationTypeDTO> adminNotificationTypes;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        acbUser2 = new JWTAuthenticatedUser();
        acbUser2.setFullName("Test");
        acbUser2.setId(3L);
        acbUser2.setFriendlyName("User");
        acbUser2.setSubjectName("TESTUSER");
        acbUser2.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(3L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));
    }

    @Before
    public void getAllowedNotificationTypes() {
        adminNotificationTypes = notificationDao.getAllNotificationTypes(adminUser.getPermissions());
        assertNotNull(adminNotificationTypes);
        assertTrue(adminNotificationTypes.size() > 0);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createNotificationAsAdminUser() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> origRecipMappings = notificationManager.getAll();

        NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
        RecipientDTO recip = new RecipientDTO();
        recip.setEmailAddress("test@ainq.com");
        recip = notificationManager.createRecipient(recip);
        mapping.setRecipient(recip);
        SubscriptionDTO notification = new SubscriptionDTO();
        notification.setAcb(null);
        NotificationTypeDTO type = adminNotificationTypes.get(0);
        notification.setNotificationType(type);
        mapping.setSubscription(notification);

        NotificationTypeRecipientMapDTO addedMapping = notificationManager.addRecipientNotificationMap(mapping);
        assertNotNull(addedMapping);
        assertNotNull(addedMapping.getId());

        List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationManager.getAll();
        assertNotNull(queriedRecipients);
        assertEquals(origRecipMappings.size() + 1, queriedRecipients.size());
        RecipientWithSubscriptionsDTO queriedRecip = null;
        for (RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
            if (currRecip.getEmail().equals(recip.getEmailAddress())) {
                queriedRecip = currRecip;
            }
        }
        assertNotNull(queriedRecip);
        assertEquals(recip.getEmailAddress(), queriedRecip.getEmail());
        assertEquals(1, queriedRecip.getSubscriptions().size());
        SubscriptionDTO firstRecipientNotification = queriedRecip.getSubscriptions().get(0);
        assertNull(firstRecipientNotification.getAcb());
        assertEquals(type.getId().longValue(), firstRecipientNotification.getNotificationType().getId().longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createNotificationAsAdminUserForExistingRegistrant() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> origRecipMappings = notificationManager.getAll();

        NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
        RecipientDTO recip = new RecipientDTO();
        recip.setEmailAddress("katy@ainq.com");
        mapping.setRecipient(recip);
        SubscriptionDTO notification = new SubscriptionDTO();
        notification.setAcb(null);
        NotificationTypeDTO type = adminNotificationTypes.get(0);
        notification.setNotificationType(type);
        mapping.setSubscription(notification);

        NotificationTypeRecipientMapDTO addedMapping = notificationManager.addRecipientNotificationMap(mapping);
        assertNotNull(addedMapping);
        assertNotNull(addedMapping.getId());

        List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationManager.getAll();
        assertNotNull(queriedRecipients);
        assertEquals(origRecipMappings.size(), queriedRecipients.size());
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @Rollback(true)
    public void createNotificationAsAcbAdminUserForNotAllowedNotificationType() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);

        NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
        RecipientDTO recip = new RecipientDTO();
        recip.setEmailAddress("test@ainq.com");
        mapping.setRecipient(recip);
        SubscriptionDTO notification = new SubscriptionDTO();
        CertificationBodyDTO acb = acbDao.getById(-1L);
        notification.setAcb(acb);

        NotificationTypeDTO adminOnlyNotificationType = null;
        for (NotificationTypeDTO adminType : adminNotificationTypes) {
            adminOnlyNotificationType = adminType;
        }
        notification.setNotificationType(adminOnlyNotificationType);
        mapping.setSubscription(notification);

        notificationManager.addRecipientNotificationMap(mapping);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void getAllNotificationsAsAdminUser() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> notifications = notificationManager.getAll();

        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(1, notifications.get(0).getSubscriptions().size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void getAllNotificationsAsAcbUser() {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<RecipientWithSubscriptionsDTO> notifications = notificationManager.getAll();

        assertNotNull(notifications);
        assertEquals(0, notifications.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void getNotificationsForUserAsAdminUser() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long recipId = -1L;
        RecipientWithSubscriptionsDTO notification = notificationManager.getAllForRecipient(recipId);
        assertNotNull(notification);
        assertEquals(recipId.longValue(), notification.getId().longValue());
        assertEquals(1, notification.getSubscriptions().size());

        recipId = -1L;
        notification = notificationManager.getAllForRecipient(recipId);
        assertNotNull(notification);
        assertEquals(recipId.longValue(), notification.getId().longValue());
        assertEquals(1, notification.getSubscriptions().size());
    }

    @Test(expected = EntityRetrievalException.class)
    @Transactional
    @Rollback(true)
    public void getNotificationsForUserWithoutAcbSubscriptionsAsAcbUser() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        Long recipId = -1L;
        notificationManager.getAllForRecipient(recipId);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateRecipientEmailAddress() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<RecipientWithSubscriptionsDTO> allRecipients = notificationManager.getAll();
        String firstRecipEmail = allRecipients.get(0).getEmail();

        RecipientDTO toUpdate = notificationDao.findRecipientByEmail(firstRecipEmail);
        toUpdate.setEmailAddress("updated@ainq.com");
        RecipientDTO updated = notificationManager.updateRecipient(toUpdate);
        assertNotNull(updated);
        assertEquals(toUpdate.getId().longValue(), updated.getId().longValue());
        assertEquals(toUpdate.getEmailAddress(), updated.getEmailAddress());
    }
}
