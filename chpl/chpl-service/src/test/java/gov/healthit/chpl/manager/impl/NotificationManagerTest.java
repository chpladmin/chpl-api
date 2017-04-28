package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
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
	private static List<NotificationTypeDTO> acbNotificationTypes;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		
		acbUser = new JWTAuthenticatedUser();
		acbUser.setFirstName("Test");
		acbUser.setId(3L);
		acbUser.setLastName("User3");
		acbUser.setSubjectName("testUser3");
		acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB_ADMIN"));
		
		acbUser2 = new JWTAuthenticatedUser();
		acbUser2.setFirstName("Test");
		acbUser2.setId(3L);
		acbUser2.setLastName("User");
		acbUser2.setSubjectName("TESTUSER");
		acbUser2.getPermissions().add(new GrantedPermission("ROLE_ACB_ADMIN"));
		
		atlUser = new JWTAuthenticatedUser();
		atlUser.setFirstName("ATL");
		atlUser.setId(3L);
		atlUser.setLastName("User");
		atlUser.setSubjectName("atlUser");
		atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL_ADMIN"));
	}
	
	@Before
	public void getAllowedNotificationTypes() {
		adminNotificationTypes = notificationDao.getAllNotificationTypes(adminUser.getPermissions());
		assertNotNull(adminNotificationTypes);
		assertTrue(adminNotificationTypes.size() > 0);
		acbNotificationTypes = notificationDao.getAllNotificationTypes(acbUser.getPermissions());
		assertNotNull(acbNotificationTypes);
		assertTrue(acbNotificationTypes.size() > 0);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void createNotificationAsAdminUser() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<RecipientWithSubscriptionsDTO> origRecipMappings = notificationManager.getAll();

		NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
		RecipientDTO recip = new RecipientDTO();
		recip.setEmailAddress("test@ainq.com");
		mapping.setRecipient(recip);
		SubscriptionDTO notification = new SubscriptionDTO();
		notification.setAcb(null);
		NotificationTypeDTO type = adminNotificationTypes.get(0);
		notification.setNotificationType(type);
		mapping.setNotification(notification);
		
		NotificationTypeRecipientMapDTO addedMapping = notificationManager.addRecipientNotificationMap(mapping);
		assertNotNull(addedMapping);
		assertNotNull(addedMapping.getId());
		
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationManager.getAll();
		assertNotNull(queriedRecipients);
		assertEquals(origRecipMappings.size()+1, queriedRecipients.size());
		RecipientWithSubscriptionsDTO queriedRecip = null;
		for(RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
			if(currRecip.getEmail().equals(recip.getEmailAddress())) {
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
	public void createNotificationAsAdminUserForExistingRegistrant() {
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
		mapping.setNotification(notification);
		
		NotificationTypeRecipientMapDTO addedMapping = notificationManager.addRecipientNotificationMap(mapping);
		assertNotNull(addedMapping);
		assertNotNull(addedMapping.getId());
		
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationManager.getAll();
		assertNotNull(queriedRecipients);
		assertEquals(origRecipMappings.size(), queriedRecipients.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void createNotificationAsAcbAdminUserForAllowedAcb() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(acbUser);
		List<RecipientWithSubscriptionsDTO> origRecipMappings = notificationManager.getAll();
		CertificationBodyDTO acb = acbDao.getById(-1L);

		NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
		RecipientDTO recip = new RecipientDTO();
		recip.setEmailAddress("test@ainq.com");
		mapping.setRecipient(recip);
		SubscriptionDTO notification = new SubscriptionDTO();
		notification.setAcb(acb);
		NotificationTypeDTO type = acbNotificationTypes.get(0);
		notification.setNotificationType(type);
		mapping.setNotification(notification);
		
		NotificationTypeRecipientMapDTO addedMapping = notificationManager.addRecipientNotificationMap(mapping);
		assertNotNull(addedMapping);
		assertNotNull(addedMapping.getId());
		
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationManager.getAll();
		assertNotNull(queriedRecipients);
		assertEquals(origRecipMappings.size()+1, queriedRecipients.size());
		RecipientWithSubscriptionsDTO queriedRecip = null;
		for(RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
			if(currRecip.getEmail().equals(recip.getEmailAddress())) {
				queriedRecip = currRecip;
			}
		}
		assertNotNull(queriedRecip);
		assertEquals(recip.getEmailAddress(), queriedRecip.getEmail());
		assertEquals(1, queriedRecip.getSubscriptions().size());
		SubscriptionDTO firstRecipientNotification = queriedRecip.getSubscriptions().get(0);
		assertNotNull(firstRecipientNotification.getAcb());
		assertEquals(acb.getId().longValue(), firstRecipientNotification.getAcb().getId().longValue());
		assertEquals(type.getId().longValue(), firstRecipientNotification.getNotificationType().getId().longValue());
	}
	
	@Test(expected = AccessDeniedException.class)
	@Transactional
	@Rollback(true)
	public void createNotificationAsAcbAdminUserForNotAllowedAcb() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(acbUser);

		NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
		RecipientDTO recip = new RecipientDTO();
		recip.setEmailAddress("test@ainq.com");
		mapping.setRecipient(recip);
		SubscriptionDTO notification = new SubscriptionDTO();
		CertificationBodyDTO acb = acbDao.getById(-5L);
		notification.setAcb(acb);
		NotificationTypeDTO type = acbNotificationTypes.get(0);
		notification.setNotificationType(type);
		mapping.setNotification(notification);
		
		notificationManager.addRecipientNotificationMap(mapping);
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
		for(NotificationTypeDTO adminType : adminNotificationTypes) {
			boolean hasAcbMatch = false;
			for(NotificationTypeDTO acbType : acbNotificationTypes) {
				if(adminType.getId().longValue() == acbType.getId().longValue()) {
					hasAcbMatch = true;
				}
			}
			if(!hasAcbMatch) {
				adminOnlyNotificationType = adminType;
			}
		}
		notification.setNotificationType(adminOnlyNotificationType);
		mapping.setNotification(notification);
		
		notificationManager.addRecipientNotificationMap(mapping);
	}
	
	@Test(expected = AccessDeniedException.class)
	@Transactional
	@Rollback(true)
	public void createNotificationAsAtlAdminUser() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(atlUser);

		NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
		RecipientDTO recip = new RecipientDTO();
		recip.setEmailAddress("test@ainq.com");
		mapping.setRecipient(recip);
		SubscriptionDTO notification = new SubscriptionDTO();
		CertificationBodyDTO acb = acbDao.getById(-5L);
		notification.setAcb(acb);
		NotificationTypeDTO type = acbNotificationTypes.get(0);
		notification.setNotificationType(type);
		mapping.setNotification(notification);
		
		notificationManager.addRecipientNotificationMap(mapping);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	@Transactional
	@Rollback(true)
	public void createNotificationAsUnauthenticatedUser() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(null);

		NotificationTypeRecipientMapDTO mapping = new NotificationTypeRecipientMapDTO();
		RecipientDTO recip = new RecipientDTO();
		recip.setEmailAddress("test@ainq.com");
		mapping.setRecipient(recip);
		SubscriptionDTO notification = new SubscriptionDTO();
		CertificationBodyDTO acb = acbDao.getById(-5L);
		notification.setAcb(acb);
		NotificationTypeDTO type = acbNotificationTypes.get(0);
		notification.setNotificationType(type);
		mapping.setNotification(notification);
		
		notificationManager.addRecipientNotificationMap(mapping);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void updateRecipientEmailAddress() {
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
	
	@Test
	@Transactional
	@Rollback(true)
	public void deleteNotificationAsAdminUser() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		//get the one to delete
		List<RecipientWithSubscriptionsDTO> allRecipMappings = notificationManager.getAll();
		RecipientWithSubscriptionsDTO origRecipMapping = allRecipMappings.get(0);
		RecipientDTO recipToDelete = notificationDao.findRecipientByEmail(origRecipMapping.getEmail());
		SubscriptionDTO subToDelete = origRecipMapping.getSubscriptions().get(0);
		
		NotificationTypeRecipientMapDTO toDelete = new NotificationTypeRecipientMapDTO();
		toDelete.setRecipient(recipToDelete);
		toDelete.setNotification(subToDelete);
		notificationManager.deleteRecipientNotificationMap(toDelete);
		
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationManager.getAll();
		boolean foundRecip = false;
		for(RecipientWithSubscriptionsDTO queriedRecip : queriedRecipients) {
			if(queriedRecip.getId().longValue() == recipToDelete.getId().longValue()) {
				foundRecip = true;
				assertEquals(origRecipMapping.getSubscriptions().size()-1, queriedRecip.getSubscriptions().size());
			}
		}
		assertTrue(foundRecip);
	}
}