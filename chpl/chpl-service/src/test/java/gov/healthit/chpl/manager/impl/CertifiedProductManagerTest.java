package gov.healthit.chpl.manager.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestParticipant;
import gov.healthit.chpl.domain.CertificationResultTestTask;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import junit.framework.TestCase;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductManagerTest extends TestCase {
	
	@Autowired private CertifiedProductManager cpManager;
	@Autowired private CertifiedProductDetailsManager cpdManager;
	
	private static JWTAuthenticatedUser adminUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	@Test
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
	@Transactional(readOnly = true)
	public void testUpdateMeaningfulUseUsers() throws EntityCreationException, EntityRetrievalException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertifiedProductDTO dto = new CertifiedProductDTO();
		List<MeaningfulUseUser> muu = new ArrayList<MeaningfulUseUser>();
		MeaningfulUseUser u1 = new MeaningfulUseUser("CHP-024050", 10L);
		MeaningfulUseUser u2 = new MeaningfulUseUser("CHP-024051", 20L);
		muu.add(u1);
		muu.add(u2);
		List<CertifiedProductDTO> dtoResponse = new ArrayList<CertifiedProductDTO>();
		dtoResponse = cpManager.updateMeaningfulUseUsers(muu);
		assertNotNull(dtoResponse);
		assertTrue(dtoResponse.get(0).getChplProductNumber().equalsIgnoreCase("CHP-024050"));
		assertTrue(dtoResponse.get(0).getMeaningfulUseUsers() == 10L);
		assertTrue(dtoResponse.get(0).getCertificationEditionId() != null);
		assertTrue(dtoResponse.get(1).getChplProductNumber().equalsIgnoreCase("CHP-024051"));
		assertTrue(dtoResponse.get(1).getMeaningfulUseUsers() == 20L);
	}
	
	@Test
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


