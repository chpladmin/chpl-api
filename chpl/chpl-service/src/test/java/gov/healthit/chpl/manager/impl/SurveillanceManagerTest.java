package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.Iterator;

import javax.persistence.EntityNotFoundException;

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

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
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
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SurveillanceManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class SurveillanceManagerTest extends TestCase {

    @Autowired
    private SurveillanceManager survManager;

    @Autowired
    private SurveillanceDAO survDao;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;
    private static JWTAuthenticatedUser acbUser;
    private static JWTAuthenticatedUser acbUser2;
    private static JWTAuthenticatedUser atlUser;

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
        acbUser2.setId(4L);
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

    @Test
    @Transactional
    @Rollback(true)
    public void notAllowedToInsertAsAtlAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        boolean failed = false;
        try {
            Long insertedId = survManager.createSurveillance(-1L, surv);
            assertNull(insertedId);
        } catch (AccessDeniedException ex) {
            System.out.println(ex.getClass() + ": " + ex.getMessage());
            failed = true;
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
            failed = true;
        }
        assertTrue(failed);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void notAllowedToInsertAsDifferentAcbAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser2);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        boolean failed = false;
        try {
            Long insertedId = survManager.createSurveillance(-1L, surv);
            assertNull(insertedId);
        } catch (AccessDeniedException ex) {
            System.out.println(ex.getClass() + ": " + ex.getMessage());
            failed = true;
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
            failed = true;
        }
        assertTrue(failed);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void insertSurveillanceWithoutNonconformities() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Long insertedId;
        try {
            insertedId = survManager.createSurveillance(-1L, surv);
            assertNotNull(insertedId);
            Surveillance got = survManager.getById(insertedId);
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
        assertEquals(1, surv.getRequirements().size());
        SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
        assertEquals("170.314 (a)(1)", gotReq.getRequirement());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void insertSurveillanceWithNonconformities() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);

        SurveillanceRequirement req2 = new SurveillanceRequirement();
        req2.setRequirement("170.314 (a)(2)");
        reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req2.setType(reqType);
        resType = survDao.findSurveillanceResultType("Non-Conformity");
        req2.setResult(resType);
        surv.getRequirements().add(req2);

        SurveillanceNonconformity nc = new SurveillanceNonconformity();
        nc.setCapApprovalDate(new Date());
        nc.setCapMustCompleteDate(new Date());
        nc.setCapStartDate(new Date());
        nc.setDateOfDetermination(new Date());
        nc.setDeveloperExplanation("Something");
        nc.setFindings("Findings!");
        nc.setSitesPassed(2);
        nc.setNonconformityType("170.314 (a)(2)");
        nc.setSummary("summary");
        nc.setTotalSites(5);
        SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType("Open");
        nc.setStatus(ncStatus);
        req2.getNonconformities().add(nc);

        Long insertedId;
        try {
            insertedId = survManager.createSurveillance(-1L, surv);
            assertNotNull(insertedId);
            Surveillance got = survManager.getById(insertedId);
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }

        assertEquals(2, surv.getRequirements().size());
        boolean oneReqWithNcs = false;
        Iterator<SurveillanceRequirement> gotReqIter = surv.getRequirements().iterator();
        while (gotReqIter.hasNext()) {
            SurveillanceRequirement gotReq = gotReqIter.next();
            if (gotReq.getNonconformities().size() == 1) {
                oneReqWithNcs = true;
                assertEquals("170.314 (a)(2)", gotReq.getRequirement());
                assertEquals(1, gotReq.getNonconformities().size());
            }
        }
        assertTrue(oneReqWithNcs);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void deleteSurveillance() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);

        Long insertedId;
        try {
            insertedId = survManager.createSurveillance(-1L, surv);
            Surveillance insertedSurv = survManager.getById(insertedId);
            assertNotNull(insertedId);
            survManager.deleteSurveillance(-1L, insertedSurv);
            boolean failed = false;
            try {
                survManager.getById(insertedId);
            } catch (EntityNotFoundException ex) {
                failed = true;
            }
            assertTrue(failed);
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }

    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateSurveillanceAddRequirement() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);
        surv.setAuthority(Authority.ROLE_ACB);

        Long insertedId = survManager.createSurveillance(-1L, surv);
        assertNotNull(insertedId);

        Surveillance got = survManager.getById(insertedId);
        assertNotNull(got);
        assertEquals(1, got.getRequirements().size());

        SurveillanceRequirement req2 = new SurveillanceRequirement();
        req2.setRequirement("170.314 (a)(2)");
        reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req2.setType(reqType);
        resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req2.setResult(resType);
        got.getRequirements().add(req2);
        survManager.updateSurveillance(-1L, got);

        got = survManager.getById(insertedId);
        assertNotNull(got);
        assertEquals(2, got.getRequirements().size());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateSurveillanceRemoveRequirement() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);

        SurveillanceRequirement req2 = new SurveillanceRequirement();
        req2.setRequirement("170.314 (a)(2)");
        reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req2.setType(reqType);
        resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req2.setResult(resType);
        surv.getRequirements().add(req2);

        Long insertedId = survManager.createSurveillance(-1L, surv);
        assertNotNull(insertedId);

        Surveillance got = survManager.getById(insertedId);
        assertNotNull(got);
        assertEquals(2, got.getRequirements().size());

        got.getRequirements().remove(got.getRequirements().iterator().next());
        survManager.updateSurveillance(-1L, got);
        got = survManager.getById(insertedId);
        assertNotNull(got);
        assertEquals(1, got.getRequirements().size());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateSurveillanceAddNonconformity() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);
        surv.setAuthority(Authority.ROLE_ACB);

        Long insertedId = survManager.createSurveillance(-1L, surv);
        assertNotNull(insertedId);

        Surveillance got = survManager.getById(insertedId);
        SurveillanceRequirement gotReq = surv.getRequirements().iterator().next();
        assertEquals(0, gotReq.getNonconformities().size());
        SurveillanceNonconformity nc = new SurveillanceNonconformity();
        nc.setCapApprovalDate(new Date());
        nc.setCapMustCompleteDate(new Date());
        nc.setCapStartDate(new Date());
        nc.setDateOfDetermination(new Date());
        nc.setDeveloperExplanation("Something");
        nc.setFindings("Findings!");
        nc.setSitesPassed(2);
        nc.setNonconformityType("170.314 (a)(2)");
        nc.setSummary("summary");
        nc.setTotalSites(5);
        SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType("Open");
        nc.setStatus(ncStatus);
        gotReq.getNonconformities().add(nc);
        survManager.updateSurveillance(-1L, got);

        got = survManager.getById(insertedId);
        assertNotNull(got);
        assertEquals(1, got.getRequirements().size());
        gotReq = surv.getRequirements().iterator().next();
        assertEquals(1, gotReq.getNonconformities().size());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateSurveillanceRemoveNonconformity() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);

        SurveillanceNonconformity nc = new SurveillanceNonconformity();
        nc.setCapApprovalDate(new Date());
        nc.setCapMustCompleteDate(new Date());
        nc.setCapStartDate(new Date());
        nc.setDateOfDetermination(new Date());
        nc.setDeveloperExplanation("Something");
        nc.setFindings("Findings!");
        nc.setSitesPassed(2);
        nc.setNonconformityType("170.314 (a)(2)");
        nc.setSummary("summary");
        nc.setTotalSites(5);
        SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType("Open");
        nc.setStatus(ncStatus);
        req.getNonconformities().add(nc);
        surv.setAuthority(Authority.ROLE_ACB);

        Long insertedId = survManager.createSurveillance(-1L, surv);
        assertNotNull(insertedId);

        Surveillance got = survManager.getById(insertedId);
        SurveillanceRequirement gotReq = got.getRequirements().iterator().next();
        assertEquals(1, gotReq.getNonconformities().size());

        resType = survDao.findSurveillanceResultType("No Non-Conformity");
        gotReq.setResult(resType);
        gotReq.getNonconformities().clear();

        survManager.updateSurveillance(-1L, got);

        got = survManager.getById(insertedId);
        assertNotNull(got);
        assertEquals(1, got.getRequirements().size());
        gotReq = got.getRequirements().iterator().next();
        assertEquals("No Non-Conformity", gotReq.getResult().getName());
        assertEquals(0, gotReq.getNonconformities().size());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateSurveillanceEndDate() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
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

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);
        surv.setAuthority(Authority.ROLE_ACB);

        Long insertedId = survManager.createSurveillance(-1L, surv);
        assertNotNull(insertedId);

        Surveillance got = survManager.getById(insertedId);
        assertNotNull(got);
        assertNull(got.getEndDate());
        got.setEndDate(new Date());
        survManager.updateSurveillance(-1L, got);

        got = survManager.getById(insertedId);
        assertNotNull(got);
        assertNotNull(got.getEndDate());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * OCD-1810.
     * 
     * @throws EntityRetrievalException
     *             if entity can't be retrieved
     */
    @Test
    @Transactional
    @Rollback(true)
    public void siteCountsValidate() throws EntityRetrievalException {
        final int totalSites = 10;
        final int sitesTested = 12;
        final int sitePassed = 14;
        final int expectedErrors = 2;
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        Surveillance surv = new Surveillance();

        CertifiedProductDTO cpDto = cpDao.getById(1L);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(new Date());
        surv.setRandomizedSitesUsed(totalSites);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);

        SurveillanceNonconformity nc = new SurveillanceNonconformity();
        nc.setCapApprovalDate(new Date());
        nc.setCapMustCompleteDate(new Date());
        nc.setCapStartDate(new Date());
        nc.setDateOfDetermination(new Date());
        nc.setDeveloperExplanation("Something");
        nc.setFindings("Findings!");
        nc.setSitesPassed(sitePassed);
        nc.setNonconformityType("170.314 (a)(2)");
        nc.setSummary("summary");
        nc.setTotalSites(sitesTested);
        SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType("Open");
        nc.setStatus(ncStatus);
        req.getNonconformities().add(nc);

        Long insertedId;
        try {
            insertedId = survManager.createSurveillance(-1L, surv);
            assertNotNull(insertedId);
            Surveillance got = survManager.getById(insertedId);
            assertNotNull(got.getErrorMessages());
            assertEquals(expectedErrors, got.getErrorMessages().size());
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
    }
}
