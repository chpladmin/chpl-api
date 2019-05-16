package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.IdListContainer;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.exception.CertificationBodyAccessException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;

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
    private SurveillanceController surveillanceController;
    @Autowired
    private SurveillanceDAO survDao;
    @Autowired
    private CertifiedProductDAO cpDao;

    private static JWTAuthenticatedUser adminUser;
    private static JWTAuthenticatedUser acbAdmin;
    private static JWTAuthenticatedUser acbAdmin2;
    private static JWTAuthenticatedUser oncAdmin;
    private static JWTAuthenticatedUser oncAndAcb;

    private static final int RANDOMIZED_SITES_USED = 10;
    private static final int SITES_TESTED = 7;
    private static final int SITES_PASSED = 4;

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
        adminUser.getPermissions().add(new GrantedPermission(Authority.ROLE_ADMIN));

        acbAdmin = new JWTAuthenticatedUser();
        acbAdmin.setFullName("acbAdmin");
        acbAdmin.setId(3L);
        acbAdmin.setFriendlyName("User");
        acbAdmin.setSubjectName("testUser3");
        acbAdmin.getPermissions().add(new GrantedPermission(Authority.ROLE_ACB));

        acbAdmin2 = new JWTAuthenticatedUser();
        acbAdmin2.setFullName("acbAdmin2");
        acbAdmin2.setId(1L);
        acbAdmin2.setFriendlyName("User");
        acbAdmin2.setSubjectName("acbAdmin2");
        acbAdmin2.getPermissions().add(new GrantedPermission(Authority.ROLE_ACB));

        oncAdmin = new JWTAuthenticatedUser();
        oncAdmin.setFullName("oncAdmin");
        oncAdmin.setId(3L);
        oncAdmin.setFriendlyName("User");
        oncAdmin.setSubjectName("oncAdminUser");
        oncAdmin.getPermissions().add(new GrantedPermission(Authority.ROLE_ONC));

        oncAndAcb = new JWTAuthenticatedUser();
        oncAndAcb.setFullName("oncAndAcb");
        oncAndAcb.setId(1L);
        oncAndAcb.setFriendlyName("User");
        oncAndAcb.setSubjectName("oncAndAcbUser");
        oncAndAcb.getPermissions().add(new GrantedPermission(Authority.ROLE_ONC));
        oncAndAcb.getPermissions().add(new GrantedPermission(Authority.ROLE_ACB));
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
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
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
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
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
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
     * Given I am authenticated as ROLE_ADMIN and ROLE_ACB
     * Given I have authority on the ACB
     * When I create a surveillance and pass in ROLE_ACB authority to the API
     * Then openchpl.surveillance.user_permission_id matches the surveillance authority of ROLE_ACB
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance insertedSurv;
        try {
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
            Surveillance got = survManager.getById(insertedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ACB);
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
        assertEquals(1, surv.getRequirements().size());
        SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
        assertEquals("170.314 (a)(1)", gotReq.getRequirement());
    }


    /** 4.
     * Given I am authenticated as ROLE_ADMIN and ROLE_ACB
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
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
     * Given I am authenticated as ROLE_ACB
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
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
     * Then the validator adds an error that the surveillance authority must be ROLE_ADMIN or ROLE_ACB
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
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
     * When I update a surveillance with authority ROLE_ACB
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance updatedSurv;
        try {
            ResponseEntity<Surveillance> response = surveillanceController.updateSurveillance(surv);
            updatedSurv = response.getBody();
            assertNotNull(updatedSurv);
            Surveillance got = survManager.getById(updatedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ACB);
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
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
            ResponseEntity<Surveillance> response = surveillanceController.updateSurveillance(surv);
            updatedSurv = response.getBody();
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
     * Given I am authenticated as ROLE_ACB
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
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
     * Given I am authenticated as ROLE_ACB
     * Given I have authority on the ACB
     * When I update a surveillance with authority ROLE_ACB
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance updatedSurv;
        try {
            ResponseEntity<Surveillance> response = surveillanceController.updateSurveillance(surv);
            updatedSurv = response.getBody();
            assertNotNull(updatedSurv);
            Surveillance got = survManager.getById(updatedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ACB);
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
     * When I delete a surveillance that was created by ROLE_ACB
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
        surv.setStartDate(new Date(System.currentTimeMillis() - 1000));
        surv.setEndDate(new Date());
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance insertedSurv = null;
        try {
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
            Surveillance got = survManager.getById(insertedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ACB);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
        assertEquals(1, insertedSurv.getRequirements().size());
        assertNotNull(insertedSurv.getId());
        SurveillanceRequirement gotReq = insertedSurv.getRequirements().iterator().next();
        assertEquals("170.314 (a)(1)", gotReq.getRequirement());

        String result = null;
        try {
            SimpleExplainableAction requestBody = new SimpleExplainableAction();
            requestBody.setReason("unit test");
            ResponseEntity<String> response = surveillanceController
                    .deleteSurveillance(insertedSurv.getId(), requestBody);
            result = response.getBody();
            assertTrue(result.contains("true"));
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    @Test(expected = MissingReasonException.class)
    @Rollback
    public void test_deleteSurveillanceWithoutReason()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, ValidationException,
            SurveillanceAuthorityAccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(oncAdmin);
        Surveillance surv = new Surveillance();

        CertifiedProductDTO cpDto = cpDao.getById(1L);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(new Date(System.currentTimeMillis() - 1000));
        surv.setEndDate(new Date());
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance insertedSurv = null;
        try {
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
            Surveillance got = survManager.getById(insertedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ACB);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
        assertEquals(1, insertedSurv.getRequirements().size());
        assertNotNull(insertedSurv.getId());
        SurveillanceRequirement gotReq = insertedSurv.getRequirements().iterator().next();
        assertEquals("170.314 (a)(1)", gotReq.getRequirement());

        ResponseEntity<String> response = surveillanceController
                .deleteSurveillance(insertedSurv.getId(), null);
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
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Surveillance surv = new Surveillance();

        CertifiedProductDTO cpDto = cpDao.getById(1L);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(new Date(System.currentTimeMillis() - 1000));
        surv.setEndDate(new Date());
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ONC);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance insertedSurv = null;
        try {
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
            Surveillance got = survManager.getById(insertedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ONC);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(1, insertedSurv.getRequirements().size());
        assertNotNull(insertedSurv.getId());
        SurveillanceRequirement gotReq = insertedSurv.getRequirements().iterator().next();
        assertEquals("170.314 (a)(1)", gotReq.getRequirement());

        String result = null;
        try {
            SimpleExplainableAction requestBody = new SimpleExplainableAction();
            requestBody.setReason("unit test");
            ResponseEntity<String> response = surveillanceController
                    .deleteSurveillance(insertedSurv.getId(), requestBody);
            result = response.getBody();
            assertTrue(result.contains("true"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /** 13.
     * Given I am authenticated as ROLE_ACB
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
    @Test(expected = SurveillanceAuthorityAccessDeniedException.class)
    @Rollback
    public void test_deleteSurveillance_HaveAcbAdmin_survCreatedByOnc_returnsError()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, ValidationException, AccessDeniedException,
            SurveillanceAuthorityAccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Surveillance surv = new Surveillance();

        CertifiedProductDTO cpDto = cpDao.getById(1L);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(new Date(System.currentTimeMillis() - 1000));
        surv.setEndDate(new Date());
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ONC);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance insertedSurv = null;
        
        try {
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
            
            Surveillance got = survManager.getById(insertedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ONC);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(1, insertedSurv.getRequirements().size());
        assertNotNull(insertedSurv.getId());
        SurveillanceRequirement gotReq = insertedSurv.getRequirements().iterator().next();
        assertEquals("170.314 (a)(1)", gotReq.getRequirement());

        SecurityContextHolder.getContext().setAuthentication(acbAdmin);
        SimpleExplainableAction requestBody = new SimpleExplainableAction();
        requestBody.setReason("unit test");
        ResponseEntity<String> response = surveillanceController
                .deleteSurveillance(insertedSurv.getId(), requestBody);
    }


    /** 14.
     * Given I am authenticated as ROLE_ACB
     * Given I have authority on the ACB
     * When I delete a surveillance that was created by ROLE_ACB
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
        surv.setStartDate(new Date(System.currentTimeMillis() - 1000));
        surv.setEndDate(new Date());
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Surveillance insertedSurv = null;
        try {
            ResponseEntity<Surveillance> response = surveillanceController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
            Surveillance got = survManager.getById(insertedSurv.getId());
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
            assertEquals(surv.getAuthority(), got.getAuthority());
            assertEquals(surv.getAuthority(), Authority.ROLE_ACB);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(1, insertedSurv.getRequirements().size());
        assertNotNull(insertedSurv.getId());
        SurveillanceRequirement gotReq = insertedSurv.getRequirements().iterator().next();
        assertEquals("170.314 (a)(1)", gotReq.getRequirement());

        String result = null;
        try {
            SimpleExplainableAction requestBody = new SimpleExplainableAction();
            requestBody.setReason("unit test");
            ResponseEntity<String> response = surveillanceController
                    .deleteSurveillance(insertedSurv.getId(), requestBody);
            result = response.getBody();
            assertTrue(result.contains("true"));
        } catch (ValidationException e) {
            assertTrue(e.getErrorMessages().contains("Surveillance cannot have authority " + Authority.ROLE_ADMIN + " for a user lacking " + Authority.ROLE_ADMIN));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.createSurveillance(surv);
        } catch (ValidationException e) {
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.createSurveillance(surv);
        } catch (ValidationException e) {
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.updateSurveillance(surv);
        } catch (ValidationException e) {
            assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Start Date is required"));
        }
    }

    /**
     * Given I am authenticated as ACB Admin.
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.updateSurveillance(surv);
        } catch (ValidationException e) {
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.updateSurveillance(surv);
        } catch (ValidationException e) {
            assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(),
                    "Date Corrective Action Plan End Date must be greater than Date Corrective Action Plan Start Date for requirement"));
        }
    }

    /**
     * Given I am authenticated as ACB Admin.
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
    public void test_updateSurveillance_violatesNonconformityClosed_blankResolution_returnsError()
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.updateSurveillance(surv);
        } catch (ValidationException e) {
            assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(),
                    "Resolution description is required"));
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.createSurveillance(surv);
        } catch (ValidationException e) {
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.createSurveillance(surv);
        } catch (ValidationException e) {
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        nc.setCapStartDate(new Date(cal.getTimeInMillis()));
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.createSurveillance(surv);
        } catch (ValidationException e) {
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
    public void test_createSurveillance_violatesNonconformityClosed_blankResolution_returnsError()
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.createSurveillance(surv);
        } catch (ValidationException e) {
            assertTrue(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(),
                    "Resolution description is required"));
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.updateSurveillance(surv);
        } catch (ValidationException e) {
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
        surv.setRandomizedSitesUsed(RANDOMIZED_SITES_USED);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(Authority.ROLE_ACB);

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
        nc.setTotalSites(SITES_TESTED);
        nc.setSitesPassed(SITES_PASSED);
        ncs.add(nc);
        req.setNonconformities(ncs);

        surv.getRequirements().add(req);

        try {
            surveillanceController.updateSurveillance(surv);
        } catch (ValidationException e) {
            assertFalse(StringUtils.containsIgnoreCase(e.getErrorMessages().toString(), "Date Corrective Action Plan Must Be Completed"));
        }
    }

    /**
     * Given I am authenticated as ACB Admin
     * Given I have authority on each surveillance
     * When I reject multiple surveillances through the API
     * Then all of the surveillances are deleted
     * @throws ObjectsMissingValidationException
     * @throws AccessDeniedException
     * @throws EntityNotFoundException
     */
    @Transactional
    @Test
    @Rollback
    public void test_deletePendingSurveillance()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException, EntityNotFoundException, AccessDeniedException, ObjectsMissingValidationException {
        SecurityContextHolder.getContext().setAuthentication(acbAdmin);
        List<Long> ids = new ArrayList<Long>(Arrays.asList(-3L, -4L, -5L, -6L, -7L, -8L, -9L, -20L, -21L, -22L));
        IdListContainer idList = new IdListContainer();
        idList.setIds(ids);
        // verify ids are in list of surveillances returned
        List<PendingSurveillanceEntity> survResults = survDao.getPendingSurveillanceByAcb(-1L);
        Boolean survHasId = false;
        for (PendingSurveillanceEntity surv : survResults) {
            for (Long id : ids) {
                if (surv.getId() == id) {
                    survHasId = true;
                    break;
                }
            }
            assertTrue(survHasId);
            survHasId = false;
        }

        // delete list of pending surveillances
        String result = surveillanceController.rejectPendingSurveillance(idList);
        assertNotNull(result);
        assertTrue(result.contains("true"));

        // verify newly deleted surveillances are deleted
        survResults.clear();
        survResults = survDao.getPendingSurveillanceByAcb(-1L);
        survHasId = false;
        for (PendingSurveillanceEntity surv : survResults) {
            for (Long id : ids) {
                if (surv.getId() == id) {
                    survHasId = true;
                    break;
                }
            }
            assertFalse(survHasId);
        }
    }

    /**
     * Given I am authenticated as ACB Admin
     * Given I have authority on each surveillance
     * Given a surveillance id has already been rejected/confirmed
     * When I reject multiple surveillances through the API
     * And one of the surveillance ids has already been rejected/confirmed
     * Then all of the valid surveillances are deleted
     * Then for the already rejected/confirmed surveillance, the API returns the related CHPL Product ID, start date, end date, and contact info of last modified user
     * @throws ObjectsMissingValidationException
     * @throws AccessDeniedException
     * @throws EntityNotFoundException
     */
    @Transactional
    @Rollback
    @Test(expected = ObjectsMissingValidationException.class)
    public void test_deletePendingSurveillance_bulk_alreadyRejected()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, ValidationException, CertificationBodyAccessException,
            UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException, EntityNotFoundException,
            AccessDeniedException, ObjectsMissingValidationException {
        SecurityContextHolder.getContext().setAuthentication(acbAdmin);
        List<Long> ids = new ArrayList<Long>(Arrays.asList(-3L, -4L, -5L, -6L, -7L, -8L, -9L, -20L, -21L, -22L));
        IdListContainer idList = new IdListContainer();
        idList.setIds(ids);
        // verify ids are in list of surveillances returned
        List<PendingSurveillanceEntity> survResults = survDao.getPendingSurveillanceByAcb(-1L);
        Boolean survHasId = false;
        for (PendingSurveillanceEntity surv : survResults) {
            for (Long id : ids) {
                if (surv.getId() == id) {
                    survHasId = true;
                    break;
                }
            }
            assertTrue(survHasId);
            survHasId = false;
        }

        // delete list of pending surveillances
        String result = surveillanceController.rejectPendingSurveillance(idList);
        assertNotNull(result);
        assertTrue(result.contains("true"));

        // verify newly deleted surveillances are deleted
        survResults.clear();
        survResults = survDao.getPendingSurveillanceByAcb(-1L);
        survHasId = false;
        for (PendingSurveillanceEntity surv : survResults) {
            for (Long id : ids) {
                if (surv.getId() == id) {
                    survHasId = true;
                    break;
                }
            }
            assertFalse(survHasId);
        }

        // try to delete already deleted pending surveillances
        try {
            result = surveillanceController.rejectPendingSurveillance(idList);
        } catch (ObjectsMissingValidationException ex) {
            for (ObjectMissingValidationException e : ex.getExceptions()) {
                assertTrue(e.getObjectId() != null); // CHPL Product ID
                assertTrue(e.getContact() != null); // contact info of last modified user
                assertTrue(e.getStartDate() != null); // Pending Surveillance start date
                assertTrue(e.getEndDate() != null); // Pending Surveillance end date
            }
        }
        result = surveillanceController.rejectPendingSurveillance(idList); // this should cause expected exception for ObjectsMissingValidationException
    }

    /**
     * Given I am authenticated as ACB Admin
     * Given I have authority on each surveillance
     * Given a surveillance id has already been rejected/confirmed
     * When I reject the same surveillance id through the API that has already been rejected/confirmed
     * Then the API returns the related CHPL Product ID, start date, end date, and contact info of last modified user
     * @throws ObjectsMissingValidationException
     * @throws AccessDeniedException
     * @throws EntityNotFoundException
     */
    @Transactional
    @Rollback
    @Test(expected = AccessDeniedException.class)
    public void test_deletePendingSurveillance_alreadyRejected()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, ValidationException, CertificationBodyAccessException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException, EntityNotFoundException, AccessDeniedException, ObjectsMissingValidationException {
        SecurityContextHolder.getContext().setAuthentication(oncAndAcb);
        Long id = -3L;
        // verify id exists
        PendingSurveillanceEntity survResult = survDao.getPendingSurveillanceById(-3L);
        assertTrue(survResult.getId() == id);

        // delete pending surveillance
        String result = surveillanceController.rejectPendingSurveillance(id);
        assertNotNull(result);
        assertTrue(result.contains("true"));

        // verify newly deleted surveillances are deleted
        survResult = survDao.getPendingSurveillanceById(-3L);
        assertNull(survResult);

        // try to delete already deleted pending surveillance
        try {
            result = surveillanceController.rejectPendingSurveillance(id);
        } catch (ObjectMissingValidationException ex) {
            assertTrue(ex.getObjectId() != null); // CHPL Product ID
            assertTrue(ex.getContact() != null); // contact info of last modified user
            assertTrue(ex.getStartDate() != null); // Pending Surveillance start date
            assertTrue(ex.getEndDate() != null); // Pending Surveillance end date
        }
        result = surveillanceController.rejectPendingSurveillance(id); // this should cause expected exception for ObjectsMissingValidationException
    }

}
