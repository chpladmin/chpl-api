package gov.healthit.chpl.web.controller;

import static org.junit.Assert.*;

import java.util.List;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.surveillance.report.AnnualReport;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class SurveillanceReportControllerAnnualReportsTest {
    private static JWTAuthenticatedUser adminUser, acbUser;

    @Autowired
    private Environment env;

    @Autowired
    private SurveillanceReportController reportController;

    @Autowired
    private FF4j ff4j;

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
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(true).when(ff4j).check(FeatureList.SURVEILLANCE_REPORTING);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void getReportByBadId() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        reportController.getAnnualReport(-100L);
    }

    @Transactional
    @Test
    public void getAllReports_NoneExist() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<AnnualReport> foundReports = reportController.getAllAnnualReports();
        assertNotNull(foundReports);
        assertEquals(0, foundReports.size());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void createReport_AllDataProvided() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long acbId = -1L;
        Integer year = 2019;
        String obstacles = "some obstacled";
        String findings = "some findings";

        AnnualReport created = createReport(acbId, year, obstacles, findings);
        assertNotNull(created);
        assertNotNull(created.getAcb());
        assertEquals(acbId, created.getAcb().getId());
        assertEquals(year, created.getYear());
        assertEquals(obstacles, created.getObstacleSummary());
        assertEquals(findings, created.getPriorityChangesFromFindingsSummary());
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void createReport_allowsNullFields()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReport created = createReport(-1L, 2019, null, null);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertNull(created.getObstacleSummary());
        assertNull(created.getPriorityChangesFromFindingsSummary());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void createReportAsAcbUserForAllowedAcb() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        Long acbId = -1L;
        Integer year = 2019;
        String obstacles = "some obstacled";
        String findings = "some findings";

        AnnualReport created = createReport(acbId, year, obstacles, findings);
        assertNotNull(created);
        assertNotNull(created.getAcb());
        assertEquals(acbId, created.getAcb().getId());
        assertEquals(year, created.getYear());
        assertEquals(obstacles, created.getObstacleSummary());
        assertEquals(findings, created.getPriorityChangesFromFindingsSummary());
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
    }

    @Transactional
    @Rollback(true)
    @Test(expected = AccessDeniedException.class)
    public void createReportAsAcbUserNotAllowedAcb() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        createReport(-2L, 2019, "", "");
    }

    @Transactional
    @Rollback(true)
    @Test(expected = InvalidArgumentsException.class)
    public void createReport_MissingYear() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        createReport(-1L, null, "", "");
    }

    @Transactional
    @Rollback(true)
    @Test(expected = InvalidArgumentsException.class)
    public void createReport_MissingAcb() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        createReport(null, 2019, "", "");
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_Obstacles()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String oldObstacles = "old obstacles";
        String newObstacles = "new obstacles!";

        AnnualReport created = createReport(-1L, 2019, oldObstacles, "");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(oldObstacles, created.getObstacleSummary());
        created.setObstacleSummary(newObstacles);
        AnnualReport updated = reportController.updateAnnualReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(newObstacles, updated.getObstacleSummary());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_Findings()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String oldFindings = "old findings";
        String newFindings = "new findings!";

        AnnualReport created = createReport(-1L, 2019, "", oldFindings);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(oldFindings, created.getPriorityChangesFromFindingsSummary());
        created.setPriorityChangesFromFindingsSummary(newFindings);
        AnnualReport updated = reportController.updateAnnualReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(newFindings, updated.getPriorityChangesFromFindingsSummary());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_allowsNullFields()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReport created = createReport(-1L, 2019, "test", "test");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        created.setObstacleSummary(null);
        created.setPriorityChangesFromFindingsSummary(null);
        AnnualReport updated = reportController.updateAnnualReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertNull(updated.getObstacleSummary());
        assertNull(updated.getPriorityChangesFromFindingsSummary());
    }

    @Transactional
    @Rollback(true)
    @Test(expected = EntityRetrievalException.class)
    public void deleteReport()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReport created = createReport(-1L, 2019, "test", "test");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        Long idToDelete = created.getId();
        reportController.deleteAnnualReport(idToDelete);
        reportController.getAnnualReport(idToDelete);
    }

    @Transactional
    @Rollback(true)
    @Test(expected = EntityRetrievalException.class)
    public void deleteReport_BadId()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        reportController.deleteAnnualReport(-100L);
    }

    private AnnualReport createReport(Long acbId, Integer year, String obstacles, String findings) throws EntityCreationException, InvalidArgumentsException {
        AnnualReport toCreate = new AnnualReport();
        CertificationBody acb = new CertificationBody();
        acb.setId(acbId);
        toCreate.setAcb(acb);
        toCreate.setYear(year);
        toCreate.setObstacleSummary(obstacles);
        toCreate.setPriorityChangesFromFindingsSummary(findings);

        AnnualReport created = reportController.createAnnualReport(toCreate);
        return created;
    }
}
