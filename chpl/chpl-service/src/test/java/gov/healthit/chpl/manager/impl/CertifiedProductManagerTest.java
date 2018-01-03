package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
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
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
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
	@Autowired private CertificationStatusDAO certStatusDao;
	@Autowired private CertifiedProductManager cpManager;
	@Autowired private CertifiedProductDetailsManager cpdManager;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
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
		testUser3.getPermissions().add(new GrantedPermission("ROLE_ACB"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Transactional
	public void testIcsFamilyTree() throws EntityRetrievalException{
		List<IcsFamilyTreeNode> tree = cpManager.getIcsFamilyTree(5L);
		assertNotNull(tree);
		assertEquals(5,tree.size());
	}
	
	@Test
	@Transactional
	public void testGet2015CertifiedProduct() throws EntityRetrievalException{
		CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(5L);
		assertNotNull(details);
		List<CertificationResult> certs = details.getCertificationResults();
		assertNotNull(certs);
		assertEquals(2, certs.size());
		
		assertNotNull(details.getSed());
		List<TestTask> testTasks = details.getSed().getTestTasks();
		assertEquals(1, testTasks.size());
		TestTask task = testTasks.get(0);
		
		boolean foundExpectedCert = false;
		for(CertificationCriterion criteria : task.getCriteria()) {
			if(criteria.getNumber().equals("170.315 (a)(1)")) {
				foundExpectedCert = true;
			}
		}
		assertTrue(foundExpectedCert);		
		assertNotNull(task.getTestParticipants());
		assertEquals(10, task.getTestParticipants().size());
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testAdminUserChangeStatusToSuspendedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByOnc.getName());
		assertNotNull(stat);
		
		Long acbId = 1L;
		Long listingId = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertificationStatusEvent statusEvent = new CertificationStatusEvent();
		statusEvent.setEventDate(System.currentTimeMillis());
		statusEvent.setStatus(new CertificationStatus(stat));
		updatedListing.getCertificationEvents().add(statusEvent);
		
		ListingUpdateRequest toUpdate = new ListingUpdateRequest();
		toUpdate.setListing(updatedListing);
		cpManager.update(acbId, toUpdate, existingListing);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		DeveloperStatusEventDTO status = dev.getStatus();
		assertNotNull(status);
		assertNotNull(status.getId());
		assertNotNull(status.getStatus());
		assertNotNull(status.getStatus().getStatusName());
		assertEquals(DeveloperStatusType.SuspendedByOnc.toString(), status.getStatus().getStatusName());
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testNonAdminUserNotAllowedToChangeStatusToSuspendedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByOnc.getName());
		assertNotNull(stat);
		
		Long acbId = 1L;
		Long listingId = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        updatedListing.getCertificationEvents().add(statusEvent);
        
		ListingUpdateRequest toUpdate = new ListingUpdateRequest();
		toUpdate.setListing(updatedListing);
		
		boolean success = true;
		try {
			cpManager.update(acbId, toUpdate, existingListing);
		} catch(AccessDeniedException adEx) {
			success = false;
		}
		assertFalse(success);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		DeveloperStatusEventDTO status = dev.getStatus();
		assertNotNull(status);
		assertNotNull(status.getId());
		assertNotNull(status.getStatus());
		assertNotNull(status.getStatus().getStatusName());
		assertEquals(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testNonAdminUserNotAllowedToChangeStatusToWithdrawnByDeveloperUnderReview() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());
		assertNotNull(stat);
		
		Long acbId = 1L;
		Long listingId = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        updatedListing.getCertificationEvents().add(statusEvent);
        
		ListingUpdateRequest toUpdate = new ListingUpdateRequest();
		toUpdate.setListing(updatedListing);
		
		boolean success = true;
		try {
			cpManager.update(acbId, toUpdate, existingListing);
		} catch(AccessDeniedException adEx) {
			success = false;
		}
		assertFalse(success);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		DeveloperStatusEventDTO status = dev.getStatus();
		assertNotNull(status);
		assertNotNull(status.getId());
		assertNotNull(status.getStatus());
		assertNotNull(status.getStatus().getStatusName());
		assertEquals(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testAdminUserChangeStatusToWithdrawnByDeveloperUnderReviewWithDeveloperBan() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());
		assertNotNull(stat);
		
		Long acbId = 1L;
		Long listingId = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        updatedListing.getCertificationEvents().add(statusEvent);

		ListingUpdateRequest toUpdate = new ListingUpdateRequest();
		toUpdate.setListing(updatedListing);
		toUpdate.setBanDeveloper(true);
		cpManager.update(acbId, toUpdate, existingListing);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		DeveloperStatusEventDTO status = dev.getStatus();
		assertNotNull(status);
		assertNotNull(status.getId());
		assertNotNull(status.getStatus());
		assertNotNull(status.getStatus().getStatusName());
		assertEquals(DeveloperStatusType.UnderCertificationBanByOnc.toString(), status.getStatus().getStatusName());
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testAdminUserChangeStatusToWithdrawnByDeveloperUnderReviewWithoutDeveloperBan() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());
		assertNotNull(stat);
		
		DeveloperDTO beforeDev = devManager.getById(-1L);
		assertNotNull(beforeDev);
		DeveloperStatusEventDTO beforeStatus = beforeDev.getStatus();
		assertNotNull(beforeStatus);
		assertNotNull(beforeStatus.getId());
		
		Long acbId = 1L;
		Long listingId = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        updatedListing.getCertificationEvents().add(statusEvent);

		ListingUpdateRequest toUpdate = new ListingUpdateRequest();
		toUpdate.setListing(updatedListing);
		toUpdate.setBanDeveloper(false);
		cpManager.update(acbId, toUpdate, existingListing);
		
		DeveloperDTO afterDev = devManager.getById(-1L);
		assertNotNull(afterDev);
		DeveloperStatusEventDTO afterStatus = afterDev.getStatus();
		assertNotNull(afterStatus);
		assertNotNull(afterStatus.getId());
		assertEquals(beforeStatus.getId().longValue(), afterStatus.getId().longValue());
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testAdminUserChangeStatusToTerminatedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.TerminatedByOnc.getName());
		assertNotNull(stat);
		
		Long acbId = 1L;
		Long listingId = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        updatedListing.getCertificationEvents().add(statusEvent);
        
		ListingUpdateRequest toUpdate = new ListingUpdateRequest();
		toUpdate.setListing(updatedListing);
		cpManager.update(acbId, toUpdate, existingListing);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		DeveloperStatusEventDTO status = dev.getStatus();
		assertNotNull(status);
		assertNotNull(status.getId());
		assertNotNull(status.getStatus());
		assertNotNull(status.getStatus().getStatusName());
		assertEquals(DeveloperStatusType.UnderCertificationBanByOnc.toString(), status.getStatus().getStatusName());
	}
	
	@Test
	@Transactional(readOnly=false)
	@Rollback(true)
	public void testNonAdminUserNotAllowedToChangeStatusToTerminatedByOnc() throws EntityRetrievalException,
		EntityCreationException, JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.TerminatedByOnc.getName());
		assertNotNull(stat);
		
		Long acbId = 1L;
		Long listingId = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        updatedListing.getCertificationEvents().add(statusEvent);
        
		ListingUpdateRequest toUpdate = new ListingUpdateRequest();
		toUpdate.setListing(updatedListing);
		boolean success = true;
		try {
			cpManager.update(acbId, toUpdate, existingListing);
		} catch(AccessDeniedException adEx) {
			success = false;
		}
		assertFalse(success);
		
		DeveloperDTO dev = devManager.getById(-1L);
		assertNotNull(dev);
		DeveloperStatusEventDTO status = dev.getStatus();
		assertNotNull(status);
		assertNotNull(status.getId());
		assertNotNull(status.getStatus());
		assertNotNull(status.getStatus().getStatusName());
		assertEquals(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());
	}
	
	/*********************
	 * QMS Standard crud tests
	 * *************************/

	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddExistingQmsStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long qmsToAdd = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origQmsLength = existingListing.getQmsStandards().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
		std.setQmsStandardId(qmsToAdd);
		std.setQmsStandardName("21 CFR Part 820");
		std.setQmsModification("None");
		std.setApplicableCriteria("All");
		updatedListing.getQmsStandards().add(std);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getQmsStandards());
		assertEquals(origQmsLength+1, updatedListing.getQmsStandards().size());
		boolean foundAddedQms = false;
		for(CertifiedProductQmsStandard qms : updatedListing.getQmsStandards()) {
			if(qms.getQmsStandardId().longValue() == qmsToAdd.longValue()) {
				foundAddedQms = true;
			}
		}
		assertTrue(foundAddedQms);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddNonExistingQmsStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origQmsLength = existingListing.getQmsStandards().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
		String stdName = "NEW QMS STANDARD";
		String stdModification = "None";
		String stdCriteria = "ALL";
		std.setQmsStandardName(stdName);
		std.setQmsModification(stdModification);
		std.setApplicableCriteria(stdCriteria);
		updatedListing.getQmsStandards().add(std);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getQmsStandards());
		assertEquals(origQmsLength+1, updatedListing.getQmsStandards().size());
		CertifiedProductQmsStandard added = updatedListing.getQmsStandards().get(0);
		assertNotNull(added.getQmsStandardId());
		assertNotNull(added.getId());
		assertEquals(stdName, added.getQmsStandardName());
		assertEquals(stdModification, added.getQmsModification());
		assertEquals(stdCriteria, added.getApplicableCriteria());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testDeleteQmsStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long qmsToAdd = 1L;
		
		//add a qms
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origQmsLength = existingListing.getQmsStandards().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
		std.setQmsStandardId(qmsToAdd);
		std.setQmsModification("None");
		std.setApplicableCriteria("All");
		updatedListing.getQmsStandards().add(std);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the qms
		CertifiedProductSearchDetails listingWithQms = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(listingWithQms.getQmsStandards());
		assertEquals(origQmsLength+1, listingWithQms.getQmsStandards().size());
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		updatedListing.getQmsStandards().clear();
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, listingWithQms);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getQmsStandards());
		assertEquals(origQmsLength, updatedListing.getQmsStandards().size());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateQmsModification() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long qmsToAdd = 1L;
		
		//add a qms
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origQmsLength = existingListing.getQmsStandards().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
		std.setQmsStandardId(qmsToAdd);
		std.setQmsModification("None");
		std.setApplicableCriteria("All");
		updatedListing.getQmsStandards().add(std);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//update the qms
		CertifiedProductSearchDetails listingWithQms = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(listingWithQms.getQmsStandards());
		assertEquals(origQmsLength+1, listingWithQms.getQmsStandards().size());
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		String newMod = "I modified a lot of stuff";
		updatedListing.getQmsStandards().get(0).setQmsModification(newMod);
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, listingWithQms);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getQmsStandards());
		assertEquals(origQmsLength+1, updatedListing.getQmsStandards().size());
		assertEquals(newMod, updatedListing.getQmsStandards().get(0).getQmsModification());
	}
	
	/*********************
	 * Targeted User crud tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddExistingTargetedUser() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long targetedUserToAdd = -1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origTuLength = existingListing.getTargetedUsers().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
		tu.setTargetedUserId(targetedUserToAdd);
		updatedListing.getTargetedUsers().add(tu);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getTargetedUsers());
		assertEquals(origTuLength+1, updatedListing.getTargetedUsers().size());
		boolean foundAddedTu = false;
		for(CertifiedProductTargetedUser updatedTu : updatedListing.getTargetedUsers()) {
			if(updatedTu.getTargetedUserId().longValue() == targetedUserToAdd.longValue()) {
				foundAddedTu = true;
			}
		}
		assertTrue(foundAddedTu);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddNonExistingTargetedUser() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origTuLength = existingListing.getTargetedUsers().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
		String newTuName = "Physical Therapy";
		tu.setTargetedUserName(newTuName);
		updatedListing.getTargetedUsers().add(tu);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getTargetedUsers());
		assertEquals(origTuLength+1, updatedListing.getTargetedUsers().size());
		
		CertifiedProductTargetedUser added = updatedListing.getTargetedUsers().get(0);
		assertNotNull(added.getTargetedUserId());
		assertNotNull(added.getId());
		assertEquals(newTuName, added.getTargetedUserName());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testDeleteTargetedUser() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long targetedUserToAdd = -1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origTuLength = existingListing.getTargetedUsers().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
		tu.setTargetedUserId(targetedUserToAdd);
		updatedListing.getTargetedUsers().add(tu);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the targeted user
		CertifiedProductSearchDetails listingWithtu = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getTargetedUsers());
		assertEquals(origTuLength+1, updatedListing.getTargetedUsers().size());
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		updatedListing.getTargetedUsers().clear();
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, listingWithtu);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getTargetedUsers());
		assertEquals(origTuLength, updatedListing.getTargetedUsers().size());
	}
	
	/*********************
	 * Accessibility Standard crud tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddExistingAccessibilityStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long accStdIdToAdd = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origAccStdLength = existingListing.getAccessibilityStandards().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductAccessibilityStandard accStd = new CertifiedProductAccessibilityStandard();
		accStd.setAccessibilityStandardId(accStdIdToAdd);
		updatedListing.getAccessibilityStandards().add(accStd);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getAccessibilityStandards());
		assertEquals(origAccStdLength+1, updatedListing.getAccessibilityStandards().size());
		boolean foundAddedStd = false;
		for(CertifiedProductAccessibilityStandard updatedStd : updatedListing.getAccessibilityStandards()) {
			if(updatedStd.getAccessibilityStandardId().longValue() == accStdIdToAdd.longValue()) {
				foundAddedStd = true;
			}
		}
		assertTrue(foundAddedStd);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddNonExistingAccessibilityStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origStdLength = existingListing.getAccessibilityStandards().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductAccessibilityStandard std = new CertifiedProductAccessibilityStandard();
		String newStdName = "NEW STANDARD";
		std.setAccessibilityStandardName(newStdName);
		updatedListing.getAccessibilityStandards().add(std);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getAccessibilityStandards());
		assertEquals(origStdLength+1, updatedListing.getAccessibilityStandards().size());
		
		CertifiedProductAccessibilityStandard added = updatedListing.getAccessibilityStandards().get(0);
		assertNotNull(added.getAccessibilityStandardName());
		assertNotNull(added.getId());
		assertEquals(newStdName, added.getAccessibilityStandardName());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testDeleteAccessibilityStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long accStdIdToAdd = 1L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		int origAccStdLength = existingListing.getAccessibilityStandards().size();
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductAccessibilityStandard accStd = new CertifiedProductAccessibilityStandard();
		accStd.setAccessibilityStandardId(accStdIdToAdd);
		updatedListing.getAccessibilityStandards().add(accStd);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the accessibility standard
		CertifiedProductSearchDetails listingWithStd = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getAccessibilityStandards());
		assertEquals(origAccStdLength+1, updatedListing.getAccessibilityStandards().size());
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		updatedListing.getAccessibilityStandards().clear();
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, listingWithStd);
		
		updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing.getAccessibilityStandards());
		assertEquals(origAccStdLength, updatedListing.getAccessibilityStandards().size());
	}
	
	/*********************
	 * Certification Result add and remove tests
	 * *************************/
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateCertificationResultSuccess() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 4L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		//update one that is currently false to be true
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				cert.setSuccess(Boolean.TRUE);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertTrue(cert.isSuccess());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * Certification Result additional software tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCertificationResultAdditionalSoftware() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultAdditionalSoftware softwareToAdd = new CertificationResultAdditionalSoftware();
				softwareToAdd.setJustification("you need it");
				softwareToAdd.setName("Microsoft Windows");
				softwareToAdd.setVersion("2000");
				cert.getAdditionalSoftware().add(softwareToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getAdditionalSoftware().size());
				CertificationResultAdditionalSoftware added = cert.getAdditionalSoftware().get(0);
				assertEquals("you need it", added.getJustification());
				assertNull(added.getCertifiedProductNumber());
				assertNull(added.getCertifiedProductId());
				assertEquals("Microsoft Windows", added.getName());
				assertEquals("2000", added.getVersion());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCertificationResultAdditionalSoftwareAsCertifiedProduct() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultAdditionalSoftware softwareToAdd = new CertificationResultAdditionalSoftware();
				softwareToAdd.setCertifiedProductId(2L);
				cert.getAdditionalSoftware().add(softwareToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getAdditionalSoftware().size());
				CertificationResultAdditionalSoftware added = cert.getAdditionalSoftware().get(0);
				assertNull(added.getJustification());
				assertNotNull(added.getCertifiedProductNumber());
				assertNotNull(added.getCertifiedProductId());
				assertEquals(2, added.getCertifiedProductId().longValue());
				assertNull(added.getName());
				assertNull(added.getVersion());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCertificationResultAdditionalSoftwareWithGroupings() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultAdditionalSoftware softwareToAdd1 = new CertificationResultAdditionalSoftware();
				softwareToAdd1.setJustification("you need it");
				softwareToAdd1.setName("Microsoft Windows");
				softwareToAdd1.setVersion("2000");
				softwareToAdd1.setGrouping("A");
				cert.getAdditionalSoftware().add(softwareToAdd1);
				
				CertificationResultAdditionalSoftware softwareToAdd2 = new CertificationResultAdditionalSoftware();
				softwareToAdd2.setCertifiedProductId(2L);
				softwareToAdd2.setGrouping("A");
				cert.getAdditionalSoftware().add(softwareToAdd2);
				
				CertificationResultAdditionalSoftware softwareToAdd3 = new CertificationResultAdditionalSoftware();
				softwareToAdd3.setCertifiedProductId(3L);
				softwareToAdd3.setGrouping("B");
				cert.getAdditionalSoftware().add(softwareToAdd3);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(3, cert.getAdditionalSoftware().size());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateCertificationResultAdditionalSoftwareJustification() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultAdditionalSoftware softwareToAdd = new CertificationResultAdditionalSoftware();
				softwareToAdd.setJustification("you need it");
				softwareToAdd.setName("Microsoft Windows");
				softwareToAdd.setVersion("2000");
				cert.getAdditionalSoftware().add(softwareToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//now update the justification
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultAdditionalSoftware softwareToUpdate = cert.getAdditionalSoftware().get(0);
				softwareToUpdate.setJustification("updated justification");
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getAdditionalSoftware().size());
				CertificationResultAdditionalSoftware added = cert.getAdditionalSoftware().get(0);
				assertEquals("updated justification", added.getJustification());
				assertNull(added.getCertifiedProductNumber());
				assertNull(added.getCertifiedProductId());
				assertEquals("Microsoft Windows", added.getName());
				assertEquals("2000", added.getVersion());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultAdditionalSoftware() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 1L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				assertEquals(2, cert.getAdditionalSoftware().size());
				cert.getAdditionalSoftware().remove(0);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getAdditionalSoftware().size());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * Certification Result macra measure tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCertificationResultMacraMeasure() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 7L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				MacraMeasure measure = new MacraMeasure();
				measure.setId(1L);
				cert.getG1MacraMeasures().add(measure);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getG1MacraMeasures().size());
				MacraMeasure measure = cert.getG1MacraMeasures().get(0);
				assertEquals(1, measure.getId().longValue());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultMacraMeasure() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 7L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				MacraMeasure measure = new MacraMeasure();
				measure.setId(1L);
				cert.getG1MacraMeasures().add(measure);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the measure
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				cert.getG1MacraMeasures().clear();
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(0, cert.getG1MacraMeasures().size());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * Certification Result UCD process tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCertificationResultUcdProcess() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(toUpdateListing.getSed());
		
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				UcdProcess ucdToAdd = new UcdProcess();
				ucdToAdd.setId(1L);
				CertificationCriterion criteria = new CertificationCriterion();
				criteria.setNumber(cert.getNumber());
				ucdToAdd.getCriteria().add(criteria);
				toUpdateListing.getSed().getUcdProcesses().add(ucdToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		assertNotNull(updatedListing.getSed());
		assertNotNull(updatedListing.getSed().getUcdProcesses());
		assertEquals(1, updatedListing.getSed().getUcdProcesses().size());
		
		UcdProcess ucd = updatedListing.getSed().getUcdProcesses().get(0);
		assertNotNull(ucd.getCriteria());
		assertEquals(1, ucd.getCriteria().size());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateCertificationResultUcdProcessDetails() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				UcdProcess ucdToAdd = new UcdProcess();
				ucdToAdd.setId(1L);
				ucdToAdd.setDetails("Some details");
				CertificationCriterion criteria = new CertificationCriterion();
				criteria.setNumber(cert.getNumber());
				ucdToAdd.getCriteria().add(criteria);
				toUpdateListing.getSed().getUcdProcesses().add(ucdToAdd);			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//now update the details
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(UcdProcess ucd : toUpdateListing.getSed().getUcdProcesses()) {
			ucd.setDetails("NEW DETAILS");
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		assertNotNull(updatedListing.getSed());
		assertNotNull(updatedListing.getSed().getUcdProcesses());
		assertEquals(1, updatedListing.getSed().getUcdProcesses().size());
		UcdProcess ucd = updatedListing.getSed().getUcdProcesses().get(0);
		assertEquals("NEW DETAILS", ucd.getDetails());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultUcdProcess() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				UcdProcess ucdToAdd = new UcdProcess();
				ucdToAdd.setId(1L);
				ucdToAdd.setDetails("Some details");
				CertificationCriterion criteria = new CertificationCriterion();
				criteria.setNumber(cert.getNumber());
				ucdToAdd.getCriteria().add(criteria);
				toUpdateListing.getSed().getUcdProcesses().add(ucdToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the ucd
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing.getSed().getUcdProcesses().clear();
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		assertNotNull(updatedListing.getSed());
		assertNotNull(updatedListing.getSed().getUcdProcesses());
		assertEquals(0, updatedListing.getSed().getUcdProcesses().size());
	}
	
	/*********************
	 * Certification Result test standard tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddExistingCertificationResultTestStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestStandard tsToAdd = new CertificationResultTestStandard();
				tsToAdd.setTestStandardId(1L);
				cert.getTestStandards().add(tsToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getTestStandards().size());
				CertificationResultTestStandard added = cert.getTestStandards().get(0);
				assertEquals(1, added.getTestStandardId().longValue());
			}
		}
		assertTrue(foundCert);
	}

	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddNewCertificationResultTestStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestStandard tsToAdd = new CertificationResultTestStandard();
				tsToAdd.setTestStandardName("test test standard");
				tsToAdd.setTestStandardDescription("a very good standard");
				cert.getTestStandards().add(tsToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getTestStandards().size());
				CertificationResultTestStandard added = cert.getTestStandards().get(0);
				assertNotNull(added.getTestStandardId());
				assertEquals("test test standard", added.getTestStandardName());
				assertEquals("a very good standard", added.getTestStandardDescription());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultTestStandard() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 1L;
		Long certIdToUpdate = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestStandard tsToAdd = new CertificationResultTestStandard();
				tsToAdd.setTestStandardId(1L);
				cert.getTestStandards().add(tsToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the ucd
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				cert.getTestStandards().clear();
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(0, cert.getTestStandards().size());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * Certification Result test tool tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCertificationResultTestTool() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestTool toolToAdd = new CertificationResultTestTool();
				toolToAdd.setTestToolId(1L);
				cert.getTestToolsUsed().add(toolToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getTestToolsUsed().size());
				CertificationResultTestTool added = cert.getTestToolsUsed().get(0);
				assertEquals(1, added.getTestToolId().longValue());
			}
		}
		assertTrue(foundCert);
	}

	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultTestTool() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestTool toolToAdd = new CertificationResultTestTool();
				toolToAdd.setTestToolId(1L);
				cert.getTestToolsUsed().add(toolToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the ucd
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				cert.getTestToolsUsed().clear();
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(0, cert.getTestToolsUsed().size());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * Certification Result test data tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCertificationResultTestData() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestData tdToAdd = new CertificationResultTestData();
				TestData testData = new TestData();
                testData.setId(1L);
                tdToAdd.setTestData(testData);
				tdToAdd.setAlteration("altered");
				tdToAdd.setVersion("1.0.0");
				cert.getTestDataUsed().add(tdToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getTestDataUsed().size());
				CertificationResultTestData added = cert.getTestDataUsed().get(0);
				assertNotNull(added.getTestData());
				assertNotNull(added.getTestData().getId());
				assertEquals(1, added.getTestData().getId().longValue());
				assertEquals("altered", added.getAlteration());
				assertEquals("1.0.0", added.getVersion());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateCertificationResultTestDataAlteration() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestData tdToAdd = new CertificationResultTestData();
				TestData testData = new TestData();
                testData.setId(1L);
                tdToAdd.setTestData(testData);
				tdToAdd.setAlteration("altered");
				tdToAdd.setVersion("1.0.0");
				cert.getTestDataUsed().add(tdToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//now update the details
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestData tdToUpdate = cert.getTestDataUsed().get(0);
				tdToUpdate.setAlteration("NEW ALTERATION");
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getTestDataUsed().size());
				CertificationResultTestData updated = cert.getTestDataUsed().get(0);
				assertEquals("NEW ALTERATION", updated.getAlteration());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultTestData() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestData tdToAdd = new CertificationResultTestData();
				TestData testData = new TestData();
				testData.setId(1L);
				tdToAdd.setTestData(testData);
				tdToAdd.setAlteration("altered");
				tdToAdd.setVersion("1.0.0");
				cert.getTestDataUsed().add(tdToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the ucd
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				cert.getTestDataUsed().clear();
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(0, cert.getTestDataUsed().size());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * Certification Result test procedure tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddNewCertificationResultTestProcedure() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestProcedure procToAdd = new CertificationResultTestProcedure();
				TestProcedure testProcedure = new TestProcedure();
				testProcedure.setId(1L);
				procToAdd.setTestProcedure(testProcedure);
				procToAdd.setTestProcedureVersion("1.1.1");
				cert.getTestProcedures().add(procToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getTestProcedures().size());
				CertificationResultTestProcedure added = cert.getTestProcedures().get(0);
				assertNotNull(added.getTestProcedure());
				assertNotNull(added.getTestProcedure().getId());
				assertEquals(1, added.getTestProcedure().getId().longValue());
				assertEquals("1.1.1", added.getTestProcedureVersion());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultTestProcedure() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestProcedure procToAdd = new CertificationResultTestProcedure();
				TestProcedure testProcedure = new TestProcedure();
                testProcedure.setId(1L);
                procToAdd.setTestProcedure(testProcedure);
				procToAdd.setTestProcedureVersion("1.1.1");
				cert.getTestProcedures().add(procToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the proc
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				cert.getTestProcedures().clear();
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(0, cert.getTestProcedures().size());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * Certification Result test functionality tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddNewCertificationResultTestFunctionality() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestFunctionality funcToAdd = new CertificationResultTestFunctionality();
				funcToAdd.setTestFunctionalityId(2L);
				cert.getTestFunctionality().add(funcToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(1, cert.getTestFunctionality().size());
				CertificationResultTestFunctionality added = cert.getTestFunctionality().get(0);
				assertEquals(2, added.getTestFunctionalityId().longValue());
			}
		}
		assertTrue(foundCert);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCertificationResultTestFunctionality() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certIdToUpdate = 11L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				CertificationResultTestFunctionality funcToAdd = new CertificationResultTestFunctionality();
				funcToAdd.setTestFunctionalityId(2L);
				cert.getTestFunctionality().add(funcToAdd);
			}
		}
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		//remove the proc
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CertificationResult cert : toUpdateListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				cert.getTestFunctionality().clear();
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCert = false;
		for(CertificationResult cert : updatedListing.getCertificationResults()) {
			if(cert.getId().longValue() == certIdToUpdate.longValue()) {
				foundCert = true;
				assertEquals(0, cert.getTestFunctionality().size());
			}
		}
		assertTrue(foundCert);
	}
	
	/*********************
	 * CQM tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddCqm() throws EntityRetrievalException, EntityCreationException, 
		JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		String cqmToUpdate = "CMS163";
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		boolean updatedCqm = false;
		for(CQMResultDetails cqm : toUpdateListing.getCqmResults()) {
			if(cqm.getCmsId().equals(cqmToUpdate)) {
				assertFalse(cqm.isSuccess());
				assertTrue(cqm.getSuccessVersions() == null || cqm.getSuccessVersions().size() == 0);
				cqm.setSuccess(true);
				cqm.getSuccessVersions().add("v3");
				cqm.getSuccessVersions().add("v4");
				updatedCqm = true;
			}
		}
		assertTrue(updatedCqm);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCqm = false;
		for(CQMResultDetails cqm : updatedListing.getCqmResults()) {
			if(cqm.getCmsId().equals(cqmToUpdate)) {
				foundCqm = true;
				assertTrue(cqm.isSuccess());
				assertNotNull(cqm.getSuccessVersions());
				assertEquals(2, cqm.getSuccessVersions().size());
			}
		}
		assertTrue(foundCqm);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateCqmAddCriteria() throws EntityRetrievalException, EntityCreationException, 
		JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		String cqmToUpdate = "CMS163";
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		boolean updatedCqm = false;
		for(CQMResultDetails cqm : toUpdateListing.getCqmResults()) {
			if(cqm.getCmsId().equals(cqmToUpdate)) {
				assertFalse(cqm.isSuccess());
				assertTrue(cqm.getSuccessVersions() == null || cqm.getSuccessVersions().size() == 0);
				cqm.setSuccess(true);
				cqm.getSuccessVersions().add("v3");
				updatedCqm = true;
			}
		}
		assertTrue(updatedCqm);
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails listingWithCqm = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails listingWithCqmAndCriteria = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(listingWithCqmAndCriteria);
		for(CQMResultDetails cqm : listingWithCqmAndCriteria.getCqmResults()) {
			if(cqm.getCmsId().equals(cqmToUpdate)) {
				CQMResultCertification cqmCert = new CQMResultCertification();
				cqmCert.setCertificationId(25L);
				cqmCert.setCertificationNumber("170.315 (c)(1)");
				cqm.getCriteria().add(cqmCert);
			}
		}
		updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(listingWithCqmAndCriteria);
		cpManager.update(acbId, updateRequest, listingWithCqm);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCqm = false;
		for(CQMResultDetails cqm : updatedListing.getCqmResults()) {
			if(cqm.getCmsId().equals(cqmToUpdate)) {
				foundCqm = true;
				assertNotNull(cqm.getCriteria());
				assertEquals(1, cqm.getCriteria().size());
				assertEquals("170.315 (c)(1)", cqm.getCriteria().get(0).getCertificationNumber());
			}
		}
		assertTrue(foundCqm);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testRemoveCqm() throws EntityRetrievalException, EntityCreationException, 
		JsonProcessingException, InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 2L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		for(CQMResultDetails cqm : toUpdateListing.getCqmResults()) {
			if(cqm.isSuccess() == Boolean.TRUE) {
				cqm.setSuccess(Boolean.FALSE);
				cqm.getSuccessVersions().clear();
			}
		}
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		assertNotNull(updatedListing);
		boolean foundCqm = false;
		for(CQMResultDetails cqm : updatedListing.getCqmResults()) {
			foundCqm = true;
			assertFalse(cqm.isSuccess());
			assertTrue(cqm.getSuccessVersions() == null || cqm.getSuccessVersions().size() == 0);
		}
		assertTrue(foundCqm);
	}
	
	/*********************
	 * Certification Result test participant tests
	 * *************************/
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testAddTestTask() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
			InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certResultId = 7L;
		
		TestTask toAdd = new TestTask();
		toAdd.setId(-700L);
		toAdd.setDescription("description of the task");
		toAdd.setTaskErrors(6.5F);
		toAdd.setTaskErrorsStddev(4.5F);
		toAdd.setTaskPathDeviationObserved("123");
		toAdd.setTaskPathDeviationOptimal("0");
		toAdd.setTaskRating(5.0F);
		toAdd.setTaskRatingScale("Likert");
		toAdd.setTaskRatingStddev(1.5F);
		toAdd.setTaskSuccessAverage(95.1F);
		toAdd.setTaskSuccessStddev(90.0F);
		toAdd.setTaskTimeAvg("1.5");
		toAdd.setTaskTimeStddev("5");
		toAdd.setTaskTimeDeviationObservedAvg("10");
		toAdd.setTaskTimeDeviationOptimalAvg("1");
		CertificationCriterion crit = new CertificationCriterion();
		crit.setId(1L);
		crit.setNumber("170.315 (a)(1)");
		toAdd.getCriteria().add(crit);
		for(int i = 0; i < 10; i++) {
			TestParticipant tp = new TestParticipant();
			tp.setId((i+1)*-1000L);
			tp.setAgeRangeId(1L);
			tp.setAgeRange("0-9");
			tp.setAssistiveTechnologyNeeds("some needs");
			tp.setComputerExperienceMonths("5");
			tp.setEducationTypeId(1L);
			tp.setEducationTypeName("No high school degree");
			tp.setGender("Female");
			tp.setOccupation("Doctor");
			tp.setProductExperienceMonths("4");
			tp.setProfessionalExperienceMonths("65");
			toAdd.getTestParticipants().add(tp);
		}
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
		long origNumTasks = certTasks.size();
		certTasks.add(toAdd);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		assertEquals(origNumTasks+1, existingListing.getSed().getTestTasks().size());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateEducationForOneParticipant() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
			InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long acbId = 1L;
		Long listingId = 5L;
		Long certResultId = 7L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
		TestTask certTask = certTasks.get(0);
		Collection<TestParticipant> taskParts = certTask.getTestParticipants();
		
		TestParticipant firstPart = taskParts.iterator().next();
		final long changedParticipantId = firstPart.getId();
		firstPart.setEducationTypeId(1L);
		firstPart.setEducationTypeName("No high school degree");
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		certTasks = existingListing.getSed().getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		boolean changedParticipantExists = false;
		for(TestParticipant part : taskParts) {
			if(changedParticipantId == part.getId().longValue()) {
				changedParticipantExists = true;
				assertNotNull(part.getEducationTypeId());
				assertEquals(1, part.getEducationTypeId().longValue());
			}
		}
		assertTrue(changedParticipantExists);
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateOccupationForAllParticipants() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
		InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long listingId = 5L;
		Long acbId = -1L;
		Long certResultId = 7L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		List<CertificationResult> certs = updatedListing.getCertificationResults();
		CertificationResult certToUpdate = null;
		for(CertificationResult cert : certs) {
			if(cert.getId().equals(certResultId)) {
				certToUpdate = cert;
			}
		}
		List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
		TestTask certTask = certTasks.get(0);
		Collection<TestParticipant> taskParts = certTask.getTestParticipants();
		
		for(TestParticipant part : taskParts) {
			part.setOccupation("Teacher");
		}
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		certs = existingListing.getCertificationResults();
		for(CertificationResult cert : certs) {
			if(cert.getId().equals(certResultId)) {
				certToUpdate = cert;
			}
		}
		certTasks = existingListing.getSed().getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		assertEquals(10, taskParts.size());
		for(TestParticipant part : taskParts) {
			assertEquals("Teacher", part.getOccupation());
		}		
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateEducationForAllParticipants() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
	InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long listingId = 5L;
		Long acbId = -1L;
		Long certResultId = 7L;
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		List<CertificationResult> certs = updatedListing.getCertificationResults();
		CertificationResult certToUpdate = null;
		for(CertificationResult cert : certs) {
			if(cert.getId().equals(certResultId)) {
				certToUpdate = cert;
			}
		}
		
		List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
		TestTask certTask = certTasks.get(0);
		Collection<TestParticipant> taskParts = certTask.getTestParticipants();
		
		for(TestParticipant part : taskParts) {
			part.setEducationTypeId(2L);
			part.setEducationTypeName("High school graduate, diploma or the equivalent (for example: GED)");
		}
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		certs = existingListing.getCertificationResults();
		for(CertificationResult cert : certs) {
			if(cert.getId().equals(certResultId)) {
				certToUpdate = cert;
			}
		}
		certTasks = existingListing.getSed().getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		for(TestParticipant part : taskParts) {
			assertNotNull(part.getEducationTypeId());
			assertEquals(2, part.getEducationTypeId().longValue());
		}		
	}
	
	
	
	@Test
	@Transactional(readOnly = false)
	@Rollback
	public void testUpdateAgeRangeForAllParticipants() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
	InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long listingId = 5L;
		Long acbId = -1L;
		Long certResultId = 7L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
		List<CertificationResult> certs = toUpdateListing.getCertificationResults();
		CertificationResult certToUpdate = null;
		for(CertificationResult cert : certs) {
			if(cert.getId().equals(certResultId)) {
				certToUpdate = cert;
			}
		}
		List<TestTask> certTasks = toUpdateListing.getSed().getTestTasks();
		TestTask certTask = certTasks.get(0);
		Collection<TestParticipant> taskParts = certTask.getTestParticipants();
		
		for(TestParticipant part : taskParts) {
			part.setAgeRangeId(4L);
			part.setAgeRange("30-39");
		}
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(toUpdateListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		certs = existingListing.getCertificationResults();
		CertificationResult updatedCert = null;
		for(CertificationResult cert : certs) {
			if(cert.getId().longValue() == certToUpdate.getId().longValue()) {
				updatedCert = cert;
			}
		}
		assertNotNull(updatedCert);
		certTasks = existingListing.getSed().getTestTasks();
		certTask = certTasks.get(0);
		taskParts = certTask.getTestParticipants();
		assertEquals(10, taskParts.size());
		for(TestParticipant part : taskParts) {
			assertEquals(4, part.getAgeRangeId().longValue());
			assertEquals("30-39", part.getAgeRange());
		}		
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback(true)
	public void testUpdateG1MacraMeasures() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
	InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long listingId = 3L;
		Long acbId = -1L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		List<CertificationResult> certs = updatedListing.getCertificationResults();
		assertNotNull(certs);
		assertEquals(1, certs.size());
		CertificationResult cert = certs.get(0);
		assertNotNull(cert);
		List<MacraMeasure> measures = cert.getG1MacraMeasures();
		assertNotNull(measures);
		assertEquals(1, measures.size());
		MacraMeasure measure = measures.get(0);
		assertNotNull(measure);
		MacraMeasure newMeasure = new MacraMeasure();
		newMeasure.setId(2L);
		cert.getG1MacraMeasures().set(0, newMeasure);
		
		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		certs = existingListing.getCertificationResults();
		assertNotNull(certs);
		assertEquals(1, certs.size());
		cert = certs.get(0);
		assertNotNull(cert);
		measures = cert.getG1MacraMeasures();
		assertNotNull(measures);
		assertEquals(1, measures.size());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback(true)
	public void testAddG2Measure() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
	InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long listingId = 3L;
		Long acbId = -1L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		List<CertificationResult> certs = updatedListing.getCertificationResults();
		assertNotNull(certs);
		assertEquals(1, certs.size());
		CertificationResult cert = certs.get(0);
		assertNotNull(cert);
		
		MacraMeasure newMeasure = new MacraMeasure();
		newMeasure.setId(1L);
		cert.getG2MacraMeasures().add(newMeasure);

		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		certs = existingListing.getCertificationResults();
		assertNotNull(certs);
		assertEquals(1, certs.size());
		cert = certs.get(0);
		assertNotNull(cert);
		List<MacraMeasure> measures = cert.getG2MacraMeasures();
		assertNotNull(measures);
		assertEquals(1, measures.size());
		MacraMeasure measure = measures.get(0);
		assertEquals(1L, measure.getId().longValue());
	}
	
	@Test
	@Transactional(readOnly = false)
	@Rollback(true)
	public void testDeleteG1MacraMeasure() 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
	InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		Long listingId = 3L;
		Long acbId = -1L;
		
		CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
		
		CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
		List<CertificationResult> certs = updatedListing.getCertificationResults();
		assertNotNull(certs);
		assertEquals(1, certs.size());
		CertificationResult cert = certs.get(0);
		assertNotNull(cert);
		cert.getG1MacraMeasures().clear();

		ListingUpdateRequest updateRequest = new ListingUpdateRequest();
		updateRequest.setListing(updatedListing);
		cpManager.update(acbId, updateRequest, existingListing);
		
		existingListing = cpdManager.getCertifiedProductDetails(listingId);
		certs = existingListing.getCertificationResults();
		assertNotNull(certs);
		assertEquals(1, certs.size());
		cert = certs.get(0);
		assertNotNull(cert);
		List<MacraMeasure> measures = cert.getG1MacraMeasures();
		assertNotNull(measures);
		assertEquals(0, measures.size());
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
		MeaningfulUseUser u2 = new MeaningfulUseUser("15.01.01.1009.IC13.36.02.1.160402", 20L);
		MeaningfulUseUser u3 = new MeaningfulUseUser("14.99.01.1000.IC10.99.01.1.160403", 30L);
		muuSet.add(u1);
		muuSet.add(u2);
		muuSet.add(u3);
		MeaningfulUseUserResults results = cpManager.updateMeaningfulUseUsers(muuSet);
		assertNotNull(results);
		assertTrue(results.getMeaningfulUseUsers().get(0).getProductNumber().equalsIgnoreCase("CHP-024050"));
		assertTrue(results.getMeaningfulUseUsers().get(0).getNumberOfUsers() == 10L);
		assertTrue(results.getMeaningfulUseUsers().get(1).getProductNumber().equalsIgnoreCase("15.01.01.1009.IC13.36.02.1.160402"));
		assertTrue(results.getMeaningfulUseUsers().get(1).getNumberOfUsers() == 20L);
		assertTrue(results.getErrors().get(0).getError() != null);
		assertTrue(results.getErrors().get(0).getProductNumber().equalsIgnoreCase("14.99.01.1000.IC10.99.01.1.160403"));
		assertTrue(results.getErrors().get(0).getNumberOfUsers() == 30L);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void getListingsOwnedByProduct() throws EntityRetrievalException {
		List<CertifiedProductDetailsDTO> listings = cpManager.getByProduct(-1L);
		assertNotNull(listings);
		assertEquals(5, listings.size());
	}
}
