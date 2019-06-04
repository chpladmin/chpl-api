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
import gov.healthit.chpl.domain.surveillance.report.QuarterlyReport;
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
public class QuarterlyReportControllerTest {
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
        reportController.getQuarterlyReport(-100L);
    }

    @Transactional
    @Test
    public void getAllReports_NoneExist() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<QuarterlyReport> foundReports = reportController.getAllQuarterlyReports();
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
        String quarter = "Q1";
        String activities = "activities and outcomes";
        String pri = "prioritized element summary";
        String react = "reactive summary";
        String trans = "transparency disclosure summary";

        QuarterlyReport created = createReport(acbId, year, quarter, activities, pri, react, trans);
        assertNotNull(created);
        assertNotNull(created.getAcb());
        assertEquals(acbId, created.getAcb().getId());
        assertEquals(year, created.getYear());
        assertEquals(quarter, created.getQuarter());
        assertEquals(activities, created.getSurveillanceActivitiesAndOutcomes());
        assertEquals(pri, created.getPrioritizedElementSummary());
        assertEquals(react, created.getReactiveSummary());
        assertEquals(trans, created.getTransparencyDisclosureSummary());
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void createReport_allowsNullFields()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReport created = createReport(-1L, 2019, "Q1", null, null, null, null);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertNull(created.getPrioritizedElementSummary());
        assertNull(created.getReactiveSummary());
        assertNull(created.getTransparencyDisclosureSummary());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void createReportAsAcbUserForAllowedAcb() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        Long acbId = -1L;
        Integer year = 2019;
        String quarter = "Q1";
        String activities = "activities and outcomes";
        String pri = "prioritized element summary";
        String react = "reactive summary";
        String trans = "transparency disclosure summary";

        QuarterlyReport created = createReport(acbId, year, quarter, activities, pri, react, trans);
        assertNotNull(created);
        assertNotNull(created.getAcb());
        assertEquals(acbId, created.getAcb().getId());
        assertEquals(year, created.getYear());
        assertEquals(quarter, created.getQuarter());
        assertEquals(activities, created.getSurveillanceActivitiesAndOutcomes());
        assertEquals(pri, created.getPrioritizedElementSummary());
        assertEquals(react, created.getReactiveSummary());
        assertEquals(trans, created.getTransparencyDisclosureSummary());
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void createReportAsAcbUserNotProvidingAcb() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        Long acbId = -1L;
        Integer year = 2019;
        String quarter = "Q1";
        String activities = "activities and outcomes";
        String pri = "prioritized element summary";
        String react = "reactive summary";
        String trans = "transparency disclosure summary";

        QuarterlyReport created = createReport(null, year, quarter, activities, pri, react, trans);
        assertNotNull(created);
        assertNotNull(created.getAcb());
        assertEquals(acbId, created.getAcb().getId());
        assertEquals(year, created.getYear());
        assertEquals(quarter, created.getQuarter());
        assertEquals(activities, created.getSurveillanceActivitiesAndOutcomes());
        assertEquals(pri, created.getPrioritizedElementSummary());
        assertEquals(react, created.getReactiveSummary());
        assertEquals(trans, created.getTransparencyDisclosureSummary());
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
    }

    @Transactional
    @Rollback(true)
    @Test(expected = AccessDeniedException.class)
    public void createReportAsAcbUserNotAllowedAcb() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        createReport(-2L, 2019, "Q1", "", "", "", "");
    }

    @Transactional
    @Rollback(true)
    @Test(expected = InvalidArgumentsException.class)
    public void createReport_MissingQuarter() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        createReport(-1L, 2019, null, "", "", "", "");
    }

    @Transactional
    @Rollback(true)
    @Test(expected = InvalidArgumentsException.class)
    public void createReport_MissingYear() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        createReport(-1L, null, "Q1", "", "", "", "");
    }

    @Transactional
    @Rollback(true)
    @Test(expected = InvalidArgumentsException.class)
    public void createReport_MissingAcb() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        createReport(null, 2019, "Q1", "", "", "", "");
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_activitiesAndOutcomes()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String oldActivities = "old activities";
        String newActivities = "new activities!";

        QuarterlyReport created = createReport(-1L, 2019, "Q1", oldActivities, "", "", "");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(oldActivities, created.getSurveillanceActivitiesAndOutcomes());
        created.setSurveillanceActivitiesAndOutcomes(newActivities);
        QuarterlyReport updated = reportController.updateQuarterlyReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(newActivities, updated.getSurveillanceActivitiesAndOutcomes());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_transparencySummary()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String oldTrans = "old summary";
        String newTrans = "new summary!";

        QuarterlyReport created = createReport(-1L, 2019, "Q1", "", "", "", oldTrans);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(oldTrans, created.getTransparencyDisclosureSummary());
        created.setTransparencyDisclosureSummary(newTrans);
        QuarterlyReport updated = reportController.updateQuarterlyReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(newTrans, updated.getTransparencyDisclosureSummary());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_reactiveSummary()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String oldReact = "old summary";
        String newReact = "new summary!";

        QuarterlyReport created = createReport(-1L, 2019, "Q1", "", "", oldReact, "");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(oldReact, created.getReactiveSummary());
        created.setReactiveSummary(newReact);
        QuarterlyReport updated = reportController.updateQuarterlyReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(newReact, updated.getReactiveSummary());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_reactivePri()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String oldPri = "old pri";
        String newPri = "new pri!";

        QuarterlyReport created = createReport(-1L, 2019, "Q1", "", oldPri, "", "");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(oldPri, created.getPrioritizedElementSummary());
        created.setPrioritizedElementSummary(newPri);
        QuarterlyReport updated = reportController.updateQuarterlyReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(newPri, updated.getPrioritizedElementSummary());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void updateReport_allowsNullFields()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReport created = createReport(-1L, 2019, "Q1", "test", "test", "test", "test");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        created.setReactiveSummary(null);
        created.setTransparencyDisclosureSummary(null);
        created.setPrioritizedElementSummary(null);
        QuarterlyReport updated = reportController.updateQuarterlyReport(created);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertNull(updated.getPrioritizedElementSummary());
        assertNull(updated.getReactiveSummary());
        assertNull(updated.getTransparencyDisclosureSummary());
    }

    @Transactional
    @Rollback(true)
    @Test(expected = EntityRetrievalException.class)
    public void deleteReport()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReport created = createReport(-1L, 2019, "Q1", "test", "test", "test", "test");
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        Long idToDelete = created.getId();
        reportController.deleteQuarterlyReport(idToDelete);
        reportController.getQuarterlyReport(idToDelete);
    }

    @Transactional
    @Rollback(true)
    @Test(expected = EntityRetrievalException.class)
    public void deleteReport_BadId()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        reportController.deleteQuarterlyReport(-100L);
    }

    private QuarterlyReport createReport(Long acbId, Integer year, String quarter, 
            String activities, String pri, String react, String trans) throws EntityCreationException, InvalidArgumentsException {
        QuarterlyReport toCreate = new QuarterlyReport();
        CertificationBody acb = new CertificationBody();
        acb.setId(acbId);
        toCreate.setAcb(acb);
        toCreate.setYear(year);
        toCreate.setQuarter(quarter);
        toCreate.setSurveillanceActivitiesAndOutcomes(activities);
        toCreate.setPrioritizedElementSummary(pri);
        toCreate.setReactiveSummary(react);
        toCreate.setTransparencyDisclosureSummary(trans);

        QuarterlyReport created = reportController.createQuarterlyReport(toCreate);
        return created;
    }
}
