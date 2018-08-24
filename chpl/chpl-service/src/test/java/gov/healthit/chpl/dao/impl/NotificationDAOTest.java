package gov.healthit.chpl.dao.impl;

import java.util.List;

import javax.persistence.PersistenceException;

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

import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class NotificationDAOTest extends TestCase {

    @Autowired private NotificationDAO notificationDao;
    @Autowired private CertificationBodyDAO acbDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser, acbUser;

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
        acbUser.setSubjectName("acbUser");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void adminHasAllNotificationTypes() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(null);
        assertTrue(types.size() > 0);
        for (NotificationTypeDTO type : types) {
            assertTrue(notificationDao.hasNotificationType(type, adminUser.getPermissions()));
        }
    }

    /**
     * Ensure we can get all of the Notification Types.
     */
    @Test
    @Transactional
    @Rollback(true)
    public void getAllNotificationTypes() {
        final int expectedCount = 1;
        List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(null);
        assertNotNull(types);
        assertTrue(types.size() > 0);
        assertEquals(expectedCount, types.size());
        for (NotificationTypeDTO type : types) {
            assertNotNull(type.getPermissions());
            assertTrue(type.getPermissions().size() > 0);
            UserPermissionDTO perm = type.getPermissions().get(0);
            assertNotNull(perm);
            assertNotNull(perm.getAuthority());
        }
    }

    /**
     * Ensure we can get all of the ONC Admin notification types.
     */
    @Test
    @Transactional
    @Rollback(true)
    public void getOncAdminNotificationTypes() {
        final int expectedCount = 1;
        List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(adminUser.getPermissions());
        assertNotNull(types);
        assertTrue(types.size() > 0);
        assertEquals(expectedCount, types.size());
        for (NotificationTypeDTO type : types) {
            assertNotNull(type.getPermissions());
            assertTrue(type.getPermissions().size() > 0);
            UserPermissionDTO perm = type.getPermissions().get(0);
            assertNotNull(perm);
            assertNotNull(perm.getAuthority());
        }
    }

    @Test
    @Transactional
    @Rollback(true)
    public void getNotificationTypesForRoleWithNoNotificationTypes() {
        List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(acbUser.getPermissions());
        assertNotNull(types);
        assertEquals(0, types.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void findRecipientByEmail() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String email = "katy@ainq.com";
        RecipientDTO result = notificationDao.findRecipientByEmail(email);
        assertNotNull(result);
        assertEquals(-1L, result.getId().longValue());
        assertEquals(email, result.getEmailAddress());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void findRecipientById() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long id = -1L;
        RecipientDTO result = notificationDao.getRecipientById(id);
        assertNotNull(result);
        assertEquals(id.longValue(), result.getId().longValue());
    }

    @Test(expected = PersistenceException.class)
    @Transactional
    @Rollback(true)
    public void recipientNotCreatedWithNullEmailAddress() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        notificationDao.createRecipientEmailAddress(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void findRecipientSubscriptionsWithAdminCredentials() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> results = notificationDao
                .getAllNotificationMappings(adminUser.getPermissions(), null);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getSubscriptions().size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void findRecipientSubscriptionsByIdWithAdminCredentials() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long id = -1L;
        RecipientWithSubscriptionsDTO result = notificationDao
                .getAllNotificationMappingsForRecipient(id, adminUser.getPermissions(), null);
        assertNotNull(result);
        assertEquals(1, result.getSubscriptions().size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createRecipientWithNoNotifications() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao.getAllNotificationMappings(null, null);
        long baselineNumRecipients = queriedRecipients.size();

        String email = "test@ainq.com";
        RecipientDTO createdRecip = notificationDao.createRecipientEmailAddress(email);
        assertNotNull(createdRecip);
        assertNotNull(createdRecip.getId());
        assertTrue(createdRecip.getId().longValue() > 0);
        assertEquals(email, createdRecip.getEmailAddress());

        queriedRecipients = notificationDao.getAllNotificationMappings(null, null);
        assertNotNull(queriedRecipients);
        assertEquals(baselineNumRecipients + 1, queriedRecipients.size());
        RecipientWithSubscriptionsDTO queriedRecip = null;
        for (RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
            if (currRecip.getEmail().equals(email)) {
                queriedRecip = currRecip;
            }
        }

        assertNotNull(queriedRecip);
        assertEquals(createdRecip.getId().longValue(), queriedRecip.getId().longValue());
        assertEquals(createdRecip.getEmailAddress(), queriedRecip.getEmail());
        assertEquals(0, queriedRecip.getSubscriptions().size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createRecipientWithOneAdminNotification() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao
                .getAllNotificationMappings(adminUser.getPermissions(), null);
        long baselineNumRecipients = queriedRecipients.size();

        String email = "test@ainq.com";
        RecipientDTO createdRecip = notificationDao.createRecipientEmailAddress(email);
        assertNotNull(createdRecip);
        assertNotNull(createdRecip.getId());
        assertTrue(createdRecip.getId().longValue() > 0);
        assertEquals(email, createdRecip.getEmailAddress());

        Long createdRecipId = createdRecip.getId();
        List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(adminUser.getPermissions());
        NotificationTypeDTO type = types.get(0);

        NotificationTypeRecipientMapDTO createdMapping =
                notificationDao.createNotificationMapping(createdRecip, type, null);
        assertNotNull(createdMapping);
        assertNotNull(createdMapping.getRecipient());
        assertEquals(createdRecipId.longValue(), createdMapping.getRecipient().getId().longValue());
        assertEquals(email, createdMapping.getRecipient().getEmailAddress());
        assertNotNull(createdMapping.getSubscription());
        assertNull(createdMapping.getSubscription().getAcb());
        assertNotNull(createdMapping.getSubscription().getNotificationType());
        assertNotNull(createdMapping.getSubscription().getNotificationType().getId());
        assertNotNull(createdMapping.getSubscription().getNotificationType().getName());
        assertNotNull(createdMapping.getSubscription().getNotificationType().getDescription());
        assertEquals(type.getId().longValue(),
                createdMapping.getSubscription().getNotificationType().getId().longValue());

        queriedRecipients = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
        assertNotNull(queriedRecipients);
        assertEquals(baselineNumRecipients + 1, queriedRecipients.size());
        RecipientWithSubscriptionsDTO queriedRecip = null;
        for (RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
            if (currRecip.getEmail().equals(email)) {
                queriedRecip = currRecip;
            }
        }
        assertNotNull(queriedRecip);
        assertEquals(createdRecipId.longValue(), queriedRecip.getId().longValue());
        assertEquals(email, queriedRecip.getEmail());
        assertEquals(1, queriedRecip.getSubscriptions().size());
        SubscriptionDTO queriedNotification = queriedRecip.getSubscriptions().get(0);
        assertNull(queriedNotification.getAcb());
        assertNotNull(queriedNotification.getNotificationType());
        assertNotNull(queriedNotification.getNotificationType().getId());
        assertNotNull(queriedNotification.getNotificationType().getName());
        assertNotNull(queriedNotification.getNotificationType().getDescription());
        assertEquals(type.getId().longValue(),
                queriedNotification.getNotificationType().getId().longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateRecipientEmailAddress() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> allRecips = notificationDao
                .getAllNotificationMappings(adminUser.getPermissions(), null);
        RecipientDTO recipToUpdate = new RecipientDTO();
        recipToUpdate.setId(allRecips.get(0).getId());
        recipToUpdate.setEmailAddress(allRecips.get(0).getEmail());

        String email2 = "test2@ainq.com";
        recipToUpdate.setEmailAddress(email2);
        RecipientDTO updatedRecip = notificationDao.updateRecipient(recipToUpdate);
        assertNotNull(updatedRecip);
        assertNotNull(updatedRecip.getId());
        assertEquals(recipToUpdate.getId().longValue(), updatedRecip.getId().longValue());
        assertEquals(email2, updatedRecip.getEmailAddress());
    }

    @Test(expected = EntityRetrievalException.class)
    @Transactional
    @Rollback(true)
    public void deleteAllNotificationsForRecipient() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<RecipientWithSubscriptionsDTO> origRecipMappings = notificationDao
                .getAllNotificationMappings(adminUser.getPermissions(), null);

        RecipientWithSubscriptionsDTO origRecipMapping = origRecipMappings.get(0);
        RecipientDTO recipToDelete = notificationDao.findRecipientByEmail(origRecipMapping.getEmail());
        for (SubscriptionDTO subToDelete : origRecipMapping.getSubscriptions()) {
            notificationDao.deleteNotificationMapping(recipToDelete,
                    subToDelete.getNotificationType(), subToDelete.getAcb());
        }

        List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao
                .getAllNotificationMappings(adminUser.getPermissions(), null);
        assertNotNull(queriedRecipients);
        assertEquals(origRecipMappings.size() - 1, queriedRecipients.size());

        notificationDao.findRecipientByEmail(recipToDelete.getEmailAddress());
    }
}
