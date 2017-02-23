package gov.healthit.chpl.web.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.entity.SurveillanceEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class SurveillanceControllerTest {
	@Autowired
	SurveillanceController surveillanceController;
	@Autowired
	SurveillanceDAO surveillanceDao;
	
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
	
	/** 
	 * Tests the following when updating a surveillance:
	 * 1. null authority returns an error
	 * 2. Invalid authority returns an error
	 * 		a. Authority that does not exist (i.e. ONC_STAFF or FooBar)
	 * 		b. Authority is not ROLE_ONC_STAFF or ROLE_ACB_ADMIN
	 * 3. Valid authority updates database
	 * 		a. Authority equals ROLE_ONC_STAFF
	 * 		b. Authority equals ROLE_ACB_ADMIN
	 * @throws ValidationException 
	 */
	/**
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_updatesAuthority()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Surveillance survRequest = new Surveillance();
		survRequest.setAuthority(null);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(10L);
		cp.setEdition("2014");
		cp.setChplProductNumber("CHP-024050");
		survRequest.setCertifiedProduct(cp);
		Boolean causedValidationException = false;
		try{
			surveillanceController.updateSurveillance(survRequest);
		} catch(ValidationException e){
			causedValidationException = true;
			assertTrue(e.getErrorMessages().contains("A surveillance authority is required but was null."));
		}
		assertTrue(causedValidationException);
		
		survRequest.setAuthority("FooBar");
		causedValidationException = false;
		try{
			surveillanceController.updateSurveillance(survRequest);
		} catch(ValidationException e){
			causedValidationException = true;
			assertTrue(e.getErrorMessages().contains("User must have authority for " + Authority.ROLE_ONC_STAFF + " or " 
					+ Authority.ROLE_ACB_ADMIN));
		}
		assertTrue(causedValidationException);
		
		survRequest.setAuthority(Authority.ROLE_ONC_STAFF);
		survRequest.setStartDate(new Date());
		survRequest.setEndDate(new Date());
		survRequest.setFriendlyId("SURV01");
		survRequest.setId(-1L);
		survRequest.setSurveillanceIdToReplace("SURV01");
		SurveillanceType survType = new SurveillanceType();
		survType.setId(1L);
		survType.setName("Reactive");
		survRequest.setType(survType);
		Set<SurveillanceRequirement> requirements = new HashSet<SurveillanceRequirement>();
		SurveillanceRequirement survReq = new SurveillanceRequirement();
		survReq.setId(-1L);
		survReq.setRequirement("170.315 (h)(1)");
		List<SurveillanceNonconformity> survNCs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity NC = new SurveillanceNonconformity();
		NC.setCapApprovalDate(new Date());
		NC.setCapEndDate(new Date());
		NC.setCapMustCompleteDate(new Date());
		NC.setCapStartDate(new Date());
		NC.setDateOfDetermination(new Date());
		NC.setDeveloperExplanation("dev explanation");
		NC.setFindings("some findings");
		NC.setId(-1L);
		NC.setNonconformityType(NonconformityType.K1.getName());
		NC.setResolution("made some resolution");
		//NC.setSitesPassed(8);
		NC.setSummary("summary");
		SurveillanceNonconformityStatus ncStatus = new SurveillanceNonconformityStatus();
		ncStatus.setId(1L);
		ncStatus.setName("Open");
		NC.setStatus(ncStatus);
		survNCs.add(NC);
		survReq.setNonconformities(survNCs);
		SurveillanceResultType survResType = new SurveillanceResultType();
		survResType.setId(1L);
		survResType.setName("Non-Conformity");
		survReq.setResult(survResType);
		SurveillanceRequirementType survReqType = new SurveillanceRequirementType();
		survReqType.setId(1L);
		survReqType.setName("Certified Capability");
		survReq.setType(survReqType);
		requirements.add(survReq);
		survRequest.setRequirements(requirements);
		
		causedValidationException = false;
		try{
			surveillanceController.updateSurveillance(survRequest);
		} catch(ValidationException e){
			causedValidationException = true;
			assertFalse(e.getErrorMessages().contains("User must have authority for " + Authority.ROLE_ONC_STAFF + " or " 
					+ Authority.ROLE_ACB_ADMIN));
			assertFalse(e.getErrorMessages().contains("A surveillance authority is required but was null."));
			assertFalse(e.getErrorMessages().contains("No user permission id exists with authority "));
			
		}
		assertFalse(causedValidationException);
		SurveillanceEntity survEntity = surveillanceDao.getSurveillanceById(survRequest.getId());
		assertTrue(survEntity.getUserPermissionId().equals(7L));
		
		survRequest.setAuthority(Authority.ROLE_ACB_ADMIN);
		causedValidationException = false;
		try{
			surveillanceController.updateSurveillance(survRequest);
		} catch(ValidationException e){
			causedValidationException = true;
			assertFalse(e.getErrorMessages().contains("User must have authority for " + Authority.ROLE_ONC_STAFF + " or " 
					+ Authority.ROLE_ACB_ADMIN));
			assertFalse(e.getErrorMessages().contains("A surveillance authority is required but was null."));
			assertFalse(e.getErrorMessages().contains("No user permission id exists with authority "));
		}
		assertFalse(causedValidationException);
		survEntity = surveillanceDao.getSurveillanceById(-1L);
		assertTrue(survEntity.getUserPermissionId().equals(2L));
	}
}
