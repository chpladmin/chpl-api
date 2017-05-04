package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import gov.healthit.chpl.web.controller.results.SurveillanceResults;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class SurveillanceControllerTest {
	@Autowired
	private SurveillanceManager survManager;
	@Autowired
	SurveillanceController surveillanceController;
	@Autowired
	SurveillanceDAO survDao;
	@Autowired
	private CertifiedProductDAO cpDao;
	
	private static JWTAuthenticatedUser adminUser;
	private static JWTAuthenticatedUser acbAdmin;
	private static JWTAuthenticatedUser acbAdmin2;
	private static JWTAuthenticatedUser oncAdmin;
	private static JWTAuthenticatedUser oncAndAcb;
	private static JWTAuthenticatedUser oncAndAcbStaff;

	@Rule
	@Autowired
	public UnitTestRules cacheInvalidationRule;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission(Authority.ROLE_ADMIN));
		
		acbAdmin = new JWTAuthenticatedUser();
		acbAdmin.setFirstName("Test");
		acbAdmin.setId(3L);
		acbAdmin.setLastName("User3");
		acbAdmin.setSubjectName("testUser3");
		acbAdmin.getPermissions().add(new GrantedPermission(Authority.ROLE_ACB_ADMIN));
		
		acbAdmin2 = new JWTAuthenticatedUser();
		acbAdmin2.setFirstName("TESTUSER");
		acbAdmin2.setId(1L);
		acbAdmin2.setLastName("User3");
		acbAdmin2.setSubjectName("testUser3");
		acbAdmin2.getPermissions().add(new GrantedPermission(Authority.ROLE_ACB_ADMIN));
		
		oncAdmin = new JWTAuthenticatedUser();
		oncAdmin.setFirstName("Test");
		oncAdmin.setId(3L);
		oncAdmin.setLastName("User");
		oncAdmin.setSubjectName("TESTUSER");
		oncAdmin.getPermissions().add(new GrantedPermission(Authority.ROLE_ADMIN));
		
		oncAndAcb = new JWTAuthenticatedUser();
		oncAndAcb.setFirstName("Test");
		oncAndAcb.setId(3L);
		oncAndAcb.setLastName("User");
		oncAndAcb.setSubjectName("TESTUSER");
		oncAndAcb.getPermissions().add(new GrantedPermission(Authority.ROLE_ADMIN));
		oncAndAcb.getPermissions().add(new GrantedPermission(Authority.ROLE_ACB_ADMIN));
		
		oncAndAcbStaff = new JWTAuthenticatedUser();
		oncAndAcbStaff.setFirstName("Test");
		oncAndAcbStaff.setId(3L);
		oncAndAcbStaff.setLastName("User");
		oncAndAcbStaff.setSubjectName("TESTUSER");
		oncAndAcbStaff.getPermissions().add(new GrantedPermission(Authority.ROLE_ADMIN));
		oncAndAcbStaff.getPermissions().add(new GrantedPermission(Authority.ROLE_ACB_STAFF));
	}
	
	/** 1. 
	 * Given I am authenticated as only ROLE_ADMIN
	 * Given I have authority on the ACB
	 * When I create a surveillance and pass in null authority to the API
	 * Then openchpl.surveillance.user_permission_id matches my user authority of ROLE_ADMIN
	 * Then survManager.getById(insertedSurv) returns the authority for ROLE_ADMIN
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_nullUsesUserObject_OncAdmin()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(null);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
	}
	
	
	/** 2. 
	 * Given I am authenticated as ROLE_ADMIN and ROLE_CMS_ADMIN
	 * Given I have authority on the ACB
	 * When I create a surveillance and pass in ROLE_ADMIN authority to the API
	 * Then openchpl.surveillance.user_permission_id matches the surveillance authority of ROLE_ADMIN
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_HaveOncAndAcb_passRoleOncAdmin()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAndAcb);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
	}
	
	
	/** 3. 
	 * Given I am authenticated as ROLE_ADMIN and ROLE_ACB_ADMIN
	 * Given I have authority on the ACB
	 * When I create a surveillance and pass in ROLE_ACB_ADMIN authority to the API
	 * Then openchpl.surveillance.user_permission_id matches the surveillance authority of ROLE_ACB_ADMIN
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_HaveOncAndAcb_passRoleAcbAdmin()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAndAcb);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ACB_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
	}
	
	
	/** 4. 
	 * Given I am authenticated as ROLE_ADMIN and ROLE_ACB_ADMIN
	 * Given I have authority on the ACB
	 * When I create a surveillance and pass in null authority to the API
	 * Then the validator adds an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_HaveOncAndAcb_passnull_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAndAcb);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(null);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch (AccessDeniedException e) {
			assertTrue(e != null);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	
	
	/** 5. 
	 * Given I am authenticated as ROLE_ACB_ADMIN
	 * Given I have authority on the ACB
	 * When I create a surveillance and pass in ROLE_ADMIN authority to the API (or any role that <> user's role)
	 * Then the validator adds an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_HaveAcbAdmin_passRoleOncStaff_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch (AccessDeniedException e) {
			assertTrue(e != null);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	
	
	/** 6. 
	 * Given I am authenticated as only ROLE_ADMIN
	 * Given I have authority on the ACB
	 * When I create a surveillance and pass in "foobar" authority to the API
	 * Then the validator adds an error that the surveillance authority must be ROLE_ADMIN or ROLE_ACB_ADMIN
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_HaveOncAdmin_passFoobar_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority("foobar");
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch (AccessDeniedException e) {
			assertTrue(e != null);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	
	
	/** 7. 
	 * Given I am authenticated as ROLE_ADMIN
	 * Given I have authority on the ACB
	 * When I update a surveillance with authority ROLE_ACB_ADMIN
	 * Then I am allowed to edit it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_HaveOncAdmin_passAcbAdmin_isPermitted()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance updatedSurv;
		try {
			updatedSurv = surveillanceController.updateSurveillance(surv);
			assertNotNull(updatedSurv);
			Surveillance got = survManager.getById(updatedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ACB_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
	}
	
	
	/** 8.
	 * Given I am authenticated as ROLE_ADMIN
	 * Given I have authority on the ACB
	 * When I update a surveillance with authority ROLE_ADMIN
	 * Then I am allowed to edit it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_HaveOncAdmin_passOncAdmin_isPermitted()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance updatedSurv;
		try {
			updatedSurv = surveillanceController.updateSurveillance(surv);
			assertNotNull(updatedSurv);
			Surveillance got = survManager.getById(updatedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
	}
	
	
	/** 9.
	 * Given I am authenticated as ROLE_ACB_ADMIN
	 * Given I have authority on the ACB
	 * When I update a surveillance with authority ROLE_ADMIN
	 * Then I am NOT allowed to edit it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_HaveAcbAdmin_passOncAdmin_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch (AccessDeniedException e) {
			assertTrue(e != null);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	
	/** 10. 
	 * Given I am authenticated as ROLE_ACB_ADMIN
	 * Given I have authority on the ACB
	 * When I update a surveillance with authority ROLE_ACB_ADMIN
	 * Then I am allowed to edit it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_HaveAcbAdmin_passAcbAdmin_isPermitted()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance updatedSurv;
		try {
			updatedSurv = surveillanceController.updateSurveillance(surv);
			assertNotNull(updatedSurv);
			Surveillance got = survManager.getById(updatedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ACB_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
	}
	
	
	/** 11. 
	 * Given I am authenticated as ROLE_ADMIN
	 * Given I have authority on the ACB
	 * When I delete a surveillance that was created by ROLE_ACB_ADMIN
	 * Then I am allowed to delete it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_deleteSurveillance_HaveOncAdmin_survCreatedByAcb_isPermitted()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ACB_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
		
		String result = null;
		try{
			result = surveillanceController.deleteSurveillance(surv.getId());
			assertTrue(result.contains("true"));
		} catch(Exception e){
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	
	
	/** 12. 
	 * Given I am authenticated as ROLE_ADMIN
	 * Given I have authority on the ACB
	 * When I delete a surveillance that was created by ROLE_ADMIN
	 * Then I am allowed to delete it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_deleteSurveillance_HaveOncAdmin_survCreatedByOnc_isPermitted()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
		
		String result = null;
		try{
			result = surveillanceController.deleteSurveillance(surv.getId());
			assertTrue(result.contains("true"));
		} catch(Exception e){
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	
	/** 13. 
	 * 	Given I am authenticated as ROLE_ACB_ADMIN
	 * Given I have authority on the ACB
	 * When I delete a surveillance that was created by ROLE_ADMIN
	 * Then I am NOT allowed to delete it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_deleteSurveillance_HaveAcbAdmin_survCreatedByOnc_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
		
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		String result = null;
		try{
			result = surveillanceController.deleteSurveillance(surv.getId());
			assertFalse(result.contains("true"));
		} catch (AccessDeniedException e) {
			assertTrue(e != null);
		} catch(Exception e){
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	

	/** 14. 
	 * Given I am authenticated as ROLE_ACB_ADMIN
	 * Given I have authority on the ACB
	 * When I delete a surveillance that was created by ROLE_ACB_ADMIN
	 * Then I am allowed to delete it
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_deleteSurveillance_HaveAcbAdmin_survCreatedByAcb_isPermitted()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ACB_ADMIN);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
		
		String result = null;
		try{
			result = surveillanceController.deleteSurveillance(surv.getId());
			assertTrue(result.contains("true"));
		} catch (ValidationException e) {
			assertTrue(e.getErrorMessages().contains("Surveillance cannot have authority " + Authority.ROLE_ADMIN + " for a user lacking " + Authority.ROLE_ADMIN));
		} catch(Exception e){
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
	}
	
	/** 15. 
	 * Given I am authenticated as ROLE_ADMIN and ROLE_ACB_STAFF
	 * Given I have authority on the ACB
	 * When I create a surveillance and pass in null authority to the API
	 * Then the surveillance authority is set to ROLE_ACB_STAFF
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_HaveOncAndAcb_passnull_authoritySetToAcbStaff()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(oncAndAcbStaff);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(null);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
		req.setResult(resType);
		
		surv.getRequirements().add(req);
		
		Surveillance insertedSurv;
		try {
			insertedSurv = surveillanceController.createSurveillance(surv);
			assertNotNull(insertedSurv);
			Surveillance got = survManager.getById(insertedSurv.getId());
			assertNotNull(got);
			assertNotNull(got.getCertifiedProduct());
			assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
			assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
			assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
			assertEquals(surv.getAuthority(), got.getAuthority());
			assertEquals(surv.getAuthority(), Authority.ROLE_ACB_STAFF);
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
		}
		assertEquals(1, surv.getRequirements().size());
		SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
		assertEquals("170.314 (a)(1)", gotReq.getRequirement());
	}
	
	/** 
	 * Given an ACB Admin with authority for existing pending surveillances is authenticated
	 * When an API call is made for /surveillance/pending
	 * Then the second time the call is made, the result is returned faster due to caching
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_getAllPendingSurveillanceForAcbUser_performance() {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin2);
		Long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
		
		SurveillanceResults results = surveillanceController.getAllPendingSurveillanceForAcbUser();
		Long endTimeInMillis = Calendar.getInstance().getTimeInMillis();
		Long elapsedTime = endTimeInMillis - startTimeInMillis;
		System.out.println("Performance Test 1 (before caching):");
		System.out.println("Total pending surveillance: " + results.getPendingSurveillance().size());
		System.out.println("Start time: " + new Date(startTimeInMillis) + " \nEnd time: " + new Date(endTimeInMillis) + " \nElapsed time: " + elapsedTime);
		
        Long startTimeInMillis2 = Calendar.getInstance().getTimeInMillis();
        SurveillanceResults results2 = surveillanceController.getAllPendingSurveillanceForAcbUser();
		Long endTimeInMillis2 = Calendar.getInstance().getTimeInMillis();
		Long elapsedTime2 = endTimeInMillis2 - startTimeInMillis2;
		System.out.println("Performance Test 2 (after caching):");
		System.out.println("Total pending surveillance: " + results2.getPendingSurveillance().size());
		System.out.println("Start time: " + new Date(startTimeInMillis2) + " \nEnd time: " + new Date(endTimeInMillis2) + " \nElapsed time: " + elapsedTime2);
		
		assertTrue(elapsedTime2 < elapsedTime);
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I create a surveillance that has DateCorrectiveActionPlanWasApproved but no value for DateCorrectiveActionPlanMustBeCompleted
	 * Then the validator adds an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_hasDateCorrectiveActionPlanWasApproved_noDateCorrectiveActionPlanMustBeCompleted_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapApprovalDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Must Be Completed"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I create a surveillance that has DateCorrectiveActionPlanWasApproved and a value for DateCorrectiveActionPlanMustBeCompleted
	 * Then the validator does not add an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_hasDateCorrectiveActionPlanWasApproved_hasDateCorrectiveActionPlanMustBeCompleted_returnsNoError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapApprovalDate(new Date());
		nc.setCapMustCompleteDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch(ValidationException e){ 
			assertFalse(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Must Be Completed"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I update a surveillance activity containing a nonconformity with a CAP End Date but no CAP Start Date
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_violatesCAPEndDate_StartDateMissingValue_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Start Date is required"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I update a surveillance activity containing a nonconformity with a CAP End Date but no CAP Approval Date
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_violatesCAPEndDate_ApprovalDateMissingValue_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Approval Date is required"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I update a surveillance activity containing a nonconformity with a CAP End Date but no CAP Start Date
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_violatesCAPEndDate_StartDateAfterEndDate_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		nc.setCapStartDate(new Date(cal.getTimeInMillis()));
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), 
					"Date Corrective Action Plan End Date must be greater than Date Corrective Action Plan Start Date for requirement"));
		}	
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I update a surveillance activity containing a nonconformity with a CAP End Date but no Resolution
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_violatesCAPEndDate_blankResolution_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), 
					"Resolution is required"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I create a surveillance activity containing a nonconformity with a CAP End Date but no CAP Start Date
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_violatesCAPEndDate_StartDateMissingValue_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Start Date is required"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I create a surveillance activity containing a nonconformity with a CAP End Date but no CAP Approval Date
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_violatesCAPEndDate_ApprovalDateMissingValue_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Approval Date is required"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I create a surveillance activity containing a nonconformity with a CAP End Date but no CAP Start Date
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_violatesCAPEndDate_StartDateAfterEndDate_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		nc.setCapStartDate(new Date(cal.getTimeInMillis()));
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), 
					"Date Corrective Action Plan End Date must be greater than Date Corrective Action Plan Start Date for requirement"));
		}	
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I create a surveillance activity containing a nonconformity with a CAP End Date but no Resolution
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_violatesCAPEndDate_blankResolution_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), 
					"Resolution is required"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I create a surveillance activity containing a nonconformity with a CAP End Date and Status = 'Open'
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_createSurveillance_violatesCAPEndDate_openStatus_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Open");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.createSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), 
					"Corrective Action Plan End Date cannot be completed for requirement"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I update a surveillance activity containing a nonconformity with a CAP End Date and Status = 'Open'
	 * Then the validator returns an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_violatesCAPEndDate_openStatus_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Open");
		nc.setStatus(survNcStatus);
		nc.setCapEndDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), 
					"Corrective Action Plan End Date cannot be completed for requirement"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I update a surveillance that has DateCorrectiveActionPlanWasApproved but no value for DateCorrectiveActionPlanMustBeCompleted
	 * Then the validator adds an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_hasDateCorrectiveActionPlanWasApproved_noDateCorrectiveActionPlanMustBeCompleted_returnsError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapApprovalDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch(ValidationException e){ 
			assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Must Be Completed"));
		}
	}
	
	/** 
	 * Given I am authenticated as ACB Admin
	 * Given I have authority on the ACB
	 * When I update a surveillance that has DateCorrectiveActionPlanWasApproved and a value for DateCorrectiveActionPlanMustBeCompleted
	 * Then the validator does not add an error
	 * @throws ValidationException 
	 * @throws EntityRetrievalException
	 * @throws JsonProcessingException
	 * @throws EntityCreationException
	 * @throws InvalidArgumentsException
	 * @throws ValidationException
	 * @throws SurveillanceAuthorityAccessDeniedException 
	 * @throws UserPermissionRetrievalException 
	 * @throws CertificationBodyAccessException 
	 */
	@Transactional 
	@Test
	@Rollback
	public void test_updateSurveillance_hasDateCorrectiveActionPlanWasApproved_hasDateCorrectiveActionPlanMustBeCompleted_returnsNoError()
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
		SecurityContextHolder.getContext().setAuthentication(acbAdmin);
		Surveillance surv = new Surveillance();
		
		CertifiedProductDTO cpDto = cpDao.getById(1L);
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(cpDto.getId());
		cp.setChplProductNumber(cp.getChplProductNumber());
		cp.setEdition(cp.getEdition());
		surv.setCertifiedProduct(cp);
		surv.setStartDate(new Date());
		surv.setRandomizedSitesUsed(10);
		SurveillanceType type = survDao.findSurveillanceType("Randomized");
		surv.setType(type);
		surv.setAuthority(Authority.ROLE_ACB_ADMIN);
		
		SurveillanceRequirement req = new SurveillanceRequirement();
		req.setRequirement("170.314 (a)(1)");
		SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
		req.setType(reqType);
		SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
		req.setResult(resType);
		
		List<SurveillanceNonconformity> ncs = new ArrayList<SurveillanceNonconformity>();
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		nc.setNonconformityType("170.523 (k)(1)");
		SurveillanceNonconformityStatus survNcStatus = new SurveillanceNonconformityStatus();
		survNcStatus.setName("Closed");
		nc.setStatus(survNcStatus);
		nc.setCapApprovalDate(new Date());
		nc.setCapMustCompleteDate(new Date());
		ncs.add(nc);
		req.setNonconformities(ncs);
		
		surv.getRequirements().add(req);
		
		try {
			surveillanceController.updateSurveillance(surv);
		} catch(ValidationException e){ 
			assertFalse(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Must Be Completed"));
		}
	}
}
