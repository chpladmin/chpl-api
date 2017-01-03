package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.CacheInvalidationRule;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestParticipant;
import gov.healthit.chpl.domain.CertificationResultTestTask;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.DeveloperStatusType;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.web.controller.results.MeaningfulUseUserResults;
import junit.framework.TestCase;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductManagerTest extends TestCase {
	
	@Autowired private DeveloperManager devManager;
	@Autowired private DeveloperDAO devDao;
	@Autowired private CertificationStatusDAO certStatusDao;
	@Autowired private CertifiedProductManager cpManager;
	@Autowired private CertifiedProductDetailsManager cpdManager;
	@Autowired private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
	@Rule
    @Autowired
    public CacheInvalidationRule cacheInvalidationRule;
	
	private static JWTAuthenticatedUser adminUser;
	private static JWTAuthenticatedUser testUser3;
	
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
	}
	
	@Test
	@Transactional
	public void testGet2015CertifiedProduct() throws EntityRetrievalException{
		CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(5L);
		assertNotNull(details);
		List<CertificationResult> certs = details.getCertificationResults();
		assertNotNull(certs);
		assertEquals(1, certs.size());
		assertEquals("170.315 (a)(1)", certs.get(0).getNumber());
		CertificationResult cert = certs.get(0);
		List<CertificationResultTestTask> certTasks = cert.getTestTasks();
		assertNotNull(certTasks);
		assertEquals(1, certTasks.size());
		CertificationResultTestTask certTask = certTasks.get(0);
		List<CertificationResultTestParticipant> taskParts = certTask.getTestParticipants();
		assertNotNull(taskParts);
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testAdminUserChangeStatusToSuspendedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByOnc.getName());
		assertNotNull(stat);
		CertifiedProductDTO cp = cpManager.getById(1L);
		cp.setCertificationStatusId(stat.getId());
		cpManager.update(1L, cp);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		assertNotNull(dev.getStatus());
		assertEquals(DeveloperStatusType.SuspendedByOnc.toString(), dev.getStatus().getStatusName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testNonAdminUserNotAllowedToChangeStatusToSuspendedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByOnc.getName());
		assertNotNull(stat);
		CertifiedProductDTO cp = cpManager.getById(1L);
		cp.setCertificationStatusId(stat.getId());
		boolean success = true;
		try {
			cpManager.update(1L, cp);
		} catch(AccessDeniedException adEx) {
			success = false;
		}
		assertFalse(success);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		assertNotNull(dev.getStatus());
		assertEquals(DeveloperStatusType.Active.toString(), dev.getStatus().getStatusName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testAdminUserChangeStatusToTerminatedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.TerminatedByOnc.getName());
		assertNotNull(stat);
		CertifiedProductDTO cp = cpManager.getById(1L);
		cp.setCertificationStatusId(stat.getId());
		cpManager.update(1L, cp);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		assertNotNull(dev.getStatus());
		assertEquals(DeveloperStatusType.UnderCertificationBanByOnc.toString(), dev.getStatus().getStatusName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testNonAdminUserNotAllowedToChangeStatusToTerminatedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.TerminatedByOnc.getName());
		assertNotNull(stat);
		CertifiedProductDTO cp = cpManager.getById(1L);
		cp.setCertificationStatusId(stat.getId());
		boolean success = true;
		try {
			cpManager.update(1L, cp);
		} catch(AccessDeniedException adEx) {
			success = false;
		}
		assertFalse(success);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		assertNotNull(dev.getStatus());
		assertEquals(DeveloperStatusType.Active.toString(), dev.getStatus().getStatusName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateEducationForOneParticipant() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CertifiedProductDTO dto = new CertifiedProductDTO();
		dto.setId(5L);
		CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(5L);
		List<CertificationResult> certs = details.getCertificationResults();
		CertificationResult cert = certs.get(0);
		List<CertificationResultTestTask> certTasks = cert.getTestTasks();
		CertificationResultTestTask certTask = certTasks.get(0);
		List<CertificationResultTestParticipant> taskParts = certTask.getTestParticipants();
		
		CertificationResultTestParticipant firstPart = taskParts.get(0);
		final long changedParticipantId = firstPart.getId();
		firstPart.setEducationTypeId(1L);
		
		cpManager.updateCertifications(-1L, dto, certs);
		
		details = cpdManager.getCertifiedProductDetails(5L);
		certs = details.getCertificationResults();
		cert = certs.get(0);
		certTasks = cert.getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		boolean changedParticipantExists = false;
		for(CertificationResultTestParticipant part : taskParts) {
			if(changedParticipantId == part.getId().longValue()) {
				changedParticipantExists = true;
				assertNotNull(part.getEducationTypeId());
				assertEquals(1, part.getEducationTypeId().longValue());
			}
		}
		assertTrue(changedParticipantExists);
		
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateOccupationForAllParticipants() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CertifiedProductDTO dto = new CertifiedProductDTO();
		dto.setId(5L);
		CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(5L);
		List<CertificationResult> certs = details.getCertificationResults();
		CertificationResult cert = certs.get(0);
		List<CertificationResultTestTask> certTasks = cert.getTestTasks();
		CertificationResultTestTask certTask = certTasks.get(0);
		List<CertificationResultTestParticipant> taskParts = certTask.getTestParticipants();
		
		for(CertificationResultTestParticipant part : taskParts) {
			part.setOccupation("Teacher");
		}
		
		cpManager.updateCertifications(-1L, dto, certs);
		
		details = cpdManager.getCertifiedProductDetails(5L);
		certs = details.getCertificationResults();
		cert = certs.get(0);
		certTasks = cert.getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		assertEquals(10, taskParts.size());
		for(CertificationResultTestParticipant part : taskParts) {
			assertEquals("Teacher", part.getOccupation());
		}		
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateEducationForAllParticipants() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CertifiedProductDTO dto = new CertifiedProductDTO();
		dto.setId(5L);
		CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(5L);
		List<CertificationResult> certs = details.getCertificationResults();
		CertificationResult cert = certs.get(0);
		List<CertificationResultTestTask> certTasks = cert.getTestTasks();
		CertificationResultTestTask certTask = certTasks.get(0);
		List<CertificationResultTestParticipant> taskParts = certTask.getTestParticipants();
		
		for(CertificationResultTestParticipant part : taskParts) {
			part.setEducationTypeId(2L);
		}
		
		cpManager.updateCertifications(-1L, dto, certs);
		
		details = cpdManager.getCertifiedProductDetails(5L);
		certs = details.getCertificationResults();
		cert = certs.get(0);
		certTasks = cert.getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		for(CertificationResultTestParticipant part : taskParts) {
			assertEquals(2, part.getEducationTypeId().longValue());
		}		
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	/**
	 * Given that I am ROLE_ONC_STAFF or ROLE_ADMIN
	 * When I update a CHPL Product Number's count of meaningfulUseUsers
	 * Then the database shows the change for only the CHPL Product Number's meaningfulUseUsers
	 * @throws EntityCreationException 
	 * @throws EntityRetrievalException
	 * @throws IOException 
	 */
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateMeaningfulUseUsers() throws EntityCreationException, EntityRetrievalException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Set<MeaningfulUseUser> muu = new LinkedHashSet<MeaningfulUseUser>();
		MeaningfulUseUser u1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser u2 = new MeaningfulUseUser("CHP-024051", 20L);
		muu.add(u1);
		muu.add(u2);
		MeaningfulUseUserResults results = cpManager.updateMeaningfulUseUsers(muu);
		assertNotNull(results);
		assertTrue(results.getMeaningfulUseUsers().get(0).getProductNumber().equalsIgnoreCase("CHP-024050"));
		assertTrue(results.getMeaningfulUseUsers().get(0).getNumberOfUsers() == 10L);
		assertTrue(results.getMeaningfulUseUsers().get(1).getProductNumber().equalsIgnoreCase("CHP-024051"));
		assertTrue(results.getMeaningfulUseUsers().get(1).getNumberOfUsers() == 20L);
	}
	
	/**
	 * Given that I am ROLE_ONC_STAFF or ROLE_ADMIN
	 * When I update a CHPL Product Number's count of meaningfulUseUsers with incorrect/bad data
	 * Then the errors array is updated for that value and the other values get updated in the database
	 * @throws EntityCreationException 
	 * @throws EntityRetrievalException
	 * @throws IOException 
	 */
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateMeaningfulUseUsersWithBadData() throws EntityCreationException, EntityRetrievalException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Set<MeaningfulUseUser> muu = new LinkedHashSet<MeaningfulUseUser>();
		MeaningfulUseUser u1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser u2 = new MeaningfulUseUser("badChplProductNumber", 20L);
		muu.add(u1);
		muu.add(u2);
		MeaningfulUseUserResults results = cpManager.updateMeaningfulUseUsers(muu);
		assertNotNull(results);
		assertTrue(results.getMeaningfulUseUsers().get(0).getProductNumber().equalsIgnoreCase("CHP-024050"));
		assertTrue(results.getMeaningfulUseUsers().get(0).getNumberOfUsers() == 10L);
		assertTrue(results.getErrors().get(0).getError() != null);
		assertTrue(results.getErrors().get(0).getProductNumber().equalsIgnoreCase("badChplProductNumber"));
		assertTrue(results.getErrors().get(0).getNumberOfUsers() == 20L);
	}
	
	/**
	 * Given that I am ROLE_ONC_STAFF or ROLE_ADMIN
	 * When I update a CHPL Product Number's count of meaningfulUseUsers with incorrect/bad data for a 2014 edition CHPL product number
	 * Then the errors array is updated for that value and the other values get updated in the database
	 * @throws EntityCreationException 
	 * @throws EntityRetrievalException
	 * @throws IOException 
	 */
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateMeaningfulUseUsersWithIncorrect2014EditionChplProductNumber() throws EntityCreationException, EntityRetrievalException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Set<MeaningfulUseUser> muu = new LinkedHashSet<MeaningfulUseUser>();
		MeaningfulUseUser u1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser u2 = new MeaningfulUseUser("CHPL-024051", 20L);
		MeaningfulUseUser u3 = new MeaningfulUseUser("CHP-024051", 30L);
		muu.add(u1);
		muu.add(u2);
		muu.add(u3);
		MeaningfulUseUserResults results = cpManager.updateMeaningfulUseUsers(muu);
		assertNotNull(results);
		assertTrue(results.getMeaningfulUseUsers().get(0).getProductNumber().equalsIgnoreCase("CHP-024050"));
		assertTrue(results.getMeaningfulUseUsers().get(0).getNumberOfUsers() == 10L);
		assertTrue(results.getErrors().get(0).getError() != null);
		assertTrue(results.getErrors().get(0).getProductNumber().equalsIgnoreCase("CHPL-024051"));
		assertTrue(results.getErrors().get(0).getNumberOfUsers() == 20L);
		assertTrue(results.getMeaningfulUseUsers().get(1).getProductNumber().equalsIgnoreCase("CHP-024051"));
		assertTrue(results.getMeaningfulUseUsers().get(1).getNumberOfUsers() == 30L);
	}
	
	/**
	 * Given that I am ROLE_ONC_STAFF or ROLE_ADMIN
	 * When I update a CHPL Product Number's count of meaningfulUseUsers with incorrect/bad data for a 2015 edition CHPL product number
	 * Then the errors array is updated for that value and the other values get updated in the database
	 * @throws EntityCreationException 
	 * @throws EntityRetrievalException
	 * @throws IOException 
	 */
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateMeaningfulUseUsersWithIncorrect2015EditionChplProductNumber() throws EntityCreationException, EntityRetrievalException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Set<MeaningfulUseUser> muuSet = new LinkedHashSet<MeaningfulUseUser>();
		
		MeaningfulUseUser u1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser u2 = new MeaningfulUseUser("15.01.01.1009.EIC08.36.1.1.160402", 20L);
		MeaningfulUseUser u3 = new MeaningfulUseUser("14.99.01.1000.EIC10.99.1.1.160403", 30L);
		muuSet.add(u1);
		muuSet.add(u2);
		muuSet.add(u3);
		MeaningfulUseUserResults results = cpManager.updateMeaningfulUseUsers(muuSet);
		assertNotNull(results);
		assertTrue(results.getMeaningfulUseUsers().get(0).getProductNumber().equalsIgnoreCase("CHP-024050"));
		assertTrue(results.getMeaningfulUseUsers().get(0).getNumberOfUsers() == 10L);
		assertTrue(results.getMeaningfulUseUsers().get(1).getProductNumber().equalsIgnoreCase("15.01.01.1009.EIC08.36.1.1.160402"));
		assertTrue(results.getMeaningfulUseUsers().get(1).getNumberOfUsers() == 20L);
		assertTrue(results.getErrors().get(0).getError() != null);
		assertTrue(results.getErrors().get(0).getProductNumber().equalsIgnoreCase("14.99.01.1000.EIC10.99.1.1.160403"));
		assertTrue(results.getErrors().get(0).getNumberOfUsers() == 30L);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateAgeRangeForAllParticipants() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CertifiedProductDTO dto = new CertifiedProductDTO();
		dto.setId(5L);
		CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(5L);
		List<CertificationResult> certs = details.getCertificationResults();
		CertificationResult cert = certs.get(0);
		List<CertificationResultTestTask> certTasks = cert.getTestTasks();
		CertificationResultTestTask certTask = certTasks.get(0);
		List<CertificationResultTestParticipant> taskParts = certTask.getTestParticipants();
		
		for(CertificationResultTestParticipant part : taskParts) {
			part.setAgeRangeId(4L);
		}
		
		cpManager.updateCertifications(-1L, dto, certs);
		
		details = cpdManager.getCertifiedProductDetails(5L);
		certs = details.getCertificationResults();
		cert = certs.get(0);
		certTasks = cert.getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		assertEquals(10, taskParts.size());
		for(CertificationResultTestParticipant part : taskParts) {
			assertEquals(4, part.getAgeRangeId().longValue());
		}		
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}


