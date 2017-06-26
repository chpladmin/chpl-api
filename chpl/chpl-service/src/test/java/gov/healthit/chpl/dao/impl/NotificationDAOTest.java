package gov.healthit.chpl.dao.impl;


import java.util.ArrayList;
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

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
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
	
	private static JWTAuthenticatedUser adminUser, testUser3, atlUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		
		testUser3 = new JWTAuthenticatedUser();
		testUser3.setFirstName("Test");
		testUser3.setId(3L);
		testUser3.setLastName("User3");
		testUser3.setSubjectName("testUser3");
		testUser3.getPermissions().add(new GrantedPermission("ROLE_ACB_ADMIN"));
		
		atlUser = new JWTAuthenticatedUser();
		atlUser.setFirstName("ATL");
		atlUser.setId(3L);
		atlUser.setLastName("User");
		atlUser.setSubjectName("atlUser");
		atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL_ADMIN"));
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void adminHasAllNotificationTypes() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(null);
		assertTrue(types.size() > 0);
		for(NotificationTypeDTO type : types) {
			assertTrue(notificationDao.hasNotificationType(type, adminUser.getPermissions()));
		}
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void acbAdminHasCorrectNotificationTypes() {
		SecurityContextHolder.getContext().setAuthentication(testUser3);

		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(null);
		for(NotificationTypeDTO type : types) {
			boolean hasAcbPermission = false;
			for(UserPermissionDTO typePermission : type.getPermissions()) {
				if(typePermission.getAuthority().equals(Authority.ROLE_ACB_ADMIN)) {
					hasAcbPermission = true;
				}
			}
			if(hasAcbPermission) {
				assertTrue(notificationDao.hasNotificationType(type, testUser3.getPermissions()));
			} else {
				assertFalse(notificationDao.hasNotificationType(type, testUser3.getPermissions()));
			}
		}
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void getAllNotificationTypes() {
		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(null);
		assertNotNull(types);
		assertTrue(types.size() > 0);
		assertEquals(6, types.size());
		for(NotificationTypeDTO type : types) {
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
	public void getOncAdminNotificationTypes() {
		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(adminUser.getPermissions());
		assertNotNull(types);
		assertTrue(types.size() > 0);
		assertEquals(6, types.size());
		for(NotificationTypeDTO type : types) {
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
	public void getAcbAdminNotificationTypes() {
		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(testUser3.getPermissions());
		assertNotNull(types);
		assertTrue(types.size() > 0);
		assertEquals(3, types.size());
		for(NotificationTypeDTO type : types) {
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
		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(atlUser.getPermissions());
		assertNotNull(types);
		assertEquals(0, types.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void findRecipientByEmail() {
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
	public void findRecipientById() {
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
		List<RecipientWithSubscriptionsDTO> results = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		assertNotNull(results);
		assertEquals(2, results.size());
		for(RecipientWithSubscriptionsDTO result : results) {
			switch(result.getId().intValue()) {
			case -1:
				assertEquals(2, result.getSubscriptions().size());
				break;
			case -2:
				assertEquals(4, result.getSubscriptions().size());
				break;
			default:
				fail("Found recipient with unexpected id " + result.getId().intValue());
			}
		}
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void findRecipientSubscriptionsByIdWithAdminCredentials() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Long id = -1L;
		RecipientWithSubscriptionsDTO result = notificationDao.getAllNotificationMappingsForRecipient(id, adminUser.getPermissions(), null);
		assertNotNull(result);
		assertEquals(2, result.getSubscriptions().size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void findRecipientSubscriptionsWithAcbCredentials() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		CertificationBodyDTO acb = acbDao.getById(-1L);
		List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
		acbs.add(acb);
		List<RecipientWithSubscriptionsDTO> results = notificationDao.getAllNotificationMappings(testUser3.getPermissions(), acbs);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertNotNull(results.get(0).getSubscriptions());
		assertEquals(2, results.get(0).getSubscriptions().size());
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
		assertEquals(baselineNumRecipients+1, queriedRecipients.size());
		RecipientWithSubscriptionsDTO queriedRecip = null;
		for(RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
			if(currRecip.getEmail().equals(email)) {
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
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
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
		assertEquals(type.getId().longValue(), createdMapping.getSubscription().getNotificationType().getId().longValue());
		
		queriedRecipients = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		assertNotNull(queriedRecipients);
		assertEquals(baselineNumRecipients+1, queriedRecipients.size());
		RecipientWithSubscriptionsDTO queriedRecip = null;
		for(RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
			if(currRecip.getEmail().equals(email)) {
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
		assertEquals(type.getId().longValue(),queriedNotification.getNotificationType().getId().longValue());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void createRecipientWithOneAcbNotification() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationBodyDTO acb = acbDao.getById(-1L);
		List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
		acbs.add(acb);
		
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao.getAllNotificationMappings(testUser3.getPermissions(), acbs);
		long baselineNumRecipients = queriedRecipients.size();
		
		String email = "test@ainq.com";
		RecipientDTO createdRecip = notificationDao.createRecipientEmailAddress(email);
		assertNotNull(createdRecip);
		assertNotNull(createdRecip.getId());
		assertTrue(createdRecip.getId().longValue() > 0);
		assertEquals(email, createdRecip.getEmailAddress());
		
		Long createdRecipId = createdRecip.getId();
		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(testUser3.getPermissions());
		NotificationTypeDTO acbNotificationType = types.get(0);

		
		NotificationTypeRecipientMapDTO createdMapping = 
				notificationDao.createNotificationMapping(createdRecip, acbNotificationType, acb);
		assertNotNull(createdMapping);
		assertNotNull(createdMapping.getRecipient());
		assertEquals(createdRecipId.longValue(), createdMapping.getRecipient().getId().longValue());
		assertEquals(email, createdMapping.getRecipient().getEmailAddress());
		assertNotNull(createdMapping.getSubscription());
		assertNotNull(createdMapping.getSubscription().getAcb());
		assertEquals(acb.getId(), createdMapping.getSubscription().getAcb().getId());
		assertNotNull(createdMapping.getSubscription().getAcb().getName());
		assertNotNull(createdMapping.getSubscription().getNotificationType());
		assertNotNull(createdMapping.getSubscription().getNotificationType().getId());
		assertNotNull(createdMapping.getSubscription().getNotificationType().getName());
		assertNotNull(createdMapping.getSubscription().getNotificationType().getDescription());
		assertEquals(acbNotificationType.getId().longValue(), createdMapping.getSubscription().getNotificationType().getId().longValue());
	
		queriedRecipients = notificationDao.getAllNotificationMappings(testUser3.getPermissions(), acbs);
		assertNotNull(queriedRecipients);
		assertEquals(baselineNumRecipients+1, queriedRecipients.size());
		RecipientWithSubscriptionsDTO queriedRecip = null;
		for(RecipientWithSubscriptionsDTO currRecip : queriedRecipients) {
			if(currRecip.getEmail().equals(email)) {
				queriedRecip = currRecip;
			}
		}
		assertNotNull(queriedRecip);
		assertEquals(createdRecipId.longValue(), queriedRecip.getId().longValue());
		assertEquals(email, queriedRecip.getEmail());
		assertEquals(1, queriedRecip.getSubscriptions().size());
		SubscriptionDTO queriedNotification = queriedRecip.getSubscriptions().get(0);
		assertNotNull(queriedNotification.getAcb());
		assertEquals(acb.getId(), queriedNotification.getAcb().getId());
		assertNotNull(queriedNotification.getAcb().getName());
		assertNotNull(queriedNotification.getNotificationType());
		assertNotNull(queriedNotification.getNotificationType().getId());
		assertNotNull(queriedNotification.getNotificationType().getName());
		assertNotNull(queriedNotification.getNotificationType().getDescription());
		assertEquals(acbNotificationType.getId().longValue(),queriedNotification.getNotificationType().getId().longValue());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void createRecipientWithOneAcbNotificationAndAskForOtherAcbNotifications() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		String email = "test@ainq.com";
		RecipientDTO recip = notificationDao.createRecipientEmailAddress(email);
		List<NotificationTypeDTO> types = notificationDao.getAllNotificationTypes(testUser3.getPermissions());
		NotificationTypeDTO acbNotificationType = types.get(0);

		CertificationBodyDTO acb = acbDao.getById(-1L);
		notificationDao.createNotificationMapping(recip, acbNotificationType, acb);
		
		List<CertificationBodyDTO> otherAcbs = new ArrayList<CertificationBodyDTO>();
		CertificationBodyDTO otherAcb = acbDao.getById(-5L);
		otherAcbs.add(otherAcb);
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao.getAllNotificationMappings(testUser3.getPermissions(), otherAcbs);
		assertNotNull(queriedRecipients);
		assertEquals(0, queriedRecipients.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void createRecipientWithAcbAndOncNotifications() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationBodyDTO acb = acbDao.getById(-1L);
		List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
		acbs.add(acb);
		List<RecipientWithSubscriptionsDTO> acbQueriedRecipients = notificationDao.getAllNotificationMappings(testUser3.getPermissions(), acbs);
		long acbCountBaseline = acbQueriedRecipients.size();

		List<RecipientWithSubscriptionsDTO> adminQueriedRecipients = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		long adminCountBasline = adminQueriedRecipients.size();
		
		String email = "test@ainq.com";
		RecipientDTO recip = notificationDao.createRecipientEmailAddress(email);
		assertNotNull(recip);
		assertNotNull(recip.getId());
		assertTrue(recip.getId().longValue() > 0);
		assertEquals(email, recip.getEmailAddress());
		
		//add an acb notification type
		Long createdRecipId = recip.getId();
		List<NotificationTypeDTO> acbTypes = notificationDao.getAllNotificationTypes(testUser3.getPermissions());
		NotificationTypeDTO acbNotificationType = acbTypes.get(0);
		NotificationTypeRecipientMapDTO acbNotification = 
				notificationDao.createNotificationMapping(recip, acbNotificationType, acb);
		
		//add an admin notification type that is different from the acb type
		List<NotificationTypeDTO> adminTypes = notificationDao.getAllNotificationTypes(adminUser.getPermissions());
		int adminTypeIndex = 0;
		NotificationTypeDTO adminNotificationType = adminTypes.get(adminTypeIndex);
		while(adminNotificationType.getId().longValue() == acbNotificationType.getId().longValue()) {
			adminTypeIndex++;
			adminNotificationType = adminTypes.get(adminTypeIndex);
		}
		NotificationTypeRecipientMapDTO oncNotification = 
				notificationDao.createNotificationMapping(recip, adminNotificationType, null);
		
		//should only get the acb notification back
		acbQueriedRecipients = notificationDao.getAllNotificationMappings(testUser3.getPermissions(), acbs);
		assertNotNull(acbQueriedRecipients);
		assertEquals(acbCountBaseline+1, acbQueriedRecipients.size());
		
		//should get both notifications back
		adminQueriedRecipients = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		assertNotNull(adminQueriedRecipients);
		assertEquals(adminCountBasline+1, adminQueriedRecipients.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void updateRecipientEmailAddress() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<RecipientWithSubscriptionsDTO> allRecips = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
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

	@Test
	@Transactional
	@Rollback(true)
	public void deleteOneOfManyNotificationsForRecipient() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<RecipientWithSubscriptionsDTO> allRecipMappings = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		
		RecipientWithSubscriptionsDTO origRecipMapping = allRecipMappings.get(0);
		RecipientDTO recipToDelete = notificationDao.findRecipientByEmail(origRecipMapping.getEmail());
		SubscriptionDTO subToDelete = origRecipMapping.getSubscriptions().get(0);
		notificationDao.deleteNotificationMapping(recipToDelete, subToDelete.getNotificationType(), subToDelete.getAcb());
		
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		boolean foundRecip = false;
		for(RecipientWithSubscriptionsDTO queriedRecip : queriedRecipients) {
			if(queriedRecip.getId().longValue() == recipToDelete.getId().longValue()) {
				foundRecip = true;
				assertEquals(origRecipMapping.getSubscriptions().size()-1, queriedRecip.getSubscriptions().size());
			}
		}
		assertTrue(foundRecip);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void deleteAllNotificationsForRecipient() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<RecipientWithSubscriptionsDTO> origRecipMappings = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		
		RecipientWithSubscriptionsDTO origRecipMapping = origRecipMappings.get(0);
		RecipientDTO recipToDelete = notificationDao.findRecipientByEmail(origRecipMapping.getEmail());
		for(SubscriptionDTO subToDelete : origRecipMapping.getSubscriptions()) {
			notificationDao.deleteNotificationMapping(recipToDelete, subToDelete.getNotificationType(), subToDelete.getAcb());
		}
		
		List<RecipientWithSubscriptionsDTO> queriedRecipients = notificationDao.getAllNotificationMappings(adminUser.getPermissions(), null);
		assertNotNull(queriedRecipients);
		assertEquals(origRecipMappings.size()-1, queriedRecipients.size());
		
		RecipientDTO foundRecipient = notificationDao.findRecipientByEmail(recipToDelete.getEmailAddress());
		assertNull(foundRecipient);
	}
}
