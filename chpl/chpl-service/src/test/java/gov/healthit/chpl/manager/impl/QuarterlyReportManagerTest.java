package gov.healthit.chpl.manager.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.SurveillanceReportManager;
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
public class QuarterlyReportManagerTest extends TestCase {

    private static JWTAuthenticatedUser adminUser, oncUser, acbUser, atlUser, cmsUser;

    @Autowired
    private SurveillanceReportManager reportManager;

    @Autowired
    private QuarterlyReportDAO quarterlyReportDao;

    @Autowired
    private QuarterDAO quarterDao;

    @Autowired
    private AnnualReportDAO annualReportDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        oncUser = new JWTAuthenticatedUser();
        oncUser.setFullName("ONC User");
        oncUser.setId(6L);
        oncUser.setFriendlyName("Onc");
        oncUser.setSubjectName("oncUser");
        oncUser.getPermissions().add(new GrantedPermission("ROLE_ONC"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(4L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));

        cmsUser = new JWTAuthenticatedUser();
        cmsUser.setFullName("CMS User");
        cmsUser.setId(5L);
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsStaffUser");
        cmsUser.getPermissions().add(new GrantedPermission("ROLE_CMS_STAFF"));
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createReportTest() throws EntityCreationException, EntityRetrievalException {
        createReport();
    }

//    @Test(expected = EntityCreationException.class)
//    @Rollback(true)
//    @Transactional
//    public void createReport_alreadyExists()
//            throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException {
//        SecurityContextHolder.getContext().setAuthentication(adminUser);
//        AnnualReportDTO annualReport = createAnnualReport(-1L, 2019);
//        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
//        toCreate.setAnnualReport(annualReport);
//        toCreate.setQuarter(quarterDao.getById(1L));
//        QuarterlyReportDTO createdReport = reportManager.createQuarterlyReport(toCreate);
//        assertNotNull(createdReport);
//        assertNotNull(createdReport.getId());
//
//        //should not be allowed to create a second one with the same year, quarter, and acb
//        toCreate = new QuarterlyReportDTO();
//        toCreate.setAnnualReport(annualReport);
//        toCreate.setQuarter(quarterDao.getById(1L));
//        reportManager.createQuarterlyReport(toCreate);
//        SecurityContextHolder.getContext().setAuthentication(null);
//    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportMissingAnnualReport()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        QuarterDTO quarter = quarterDao.getById(1L);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportMissingAcb()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        QuarterDTO quarter = quarterDao.getById(1L);
        toCreate.setQuarter(quarter);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        toCreate.setAnnualReport(annualReport);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportMissingYear()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        QuarterDTO quarter = quarterDao.getById(1L);
        toCreate.setQuarter(quarter);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        toCreate.setAnnualReport(annualReport);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportMissingQuarter() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO annualReport = createAnnualReport(-1L, 2019);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportInvalidQuarter() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO annualReport = createAnnualReport(-1L, 2019);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("BOGUS");
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAnnualReportAlreadyExists()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO annualReport = createAnnualReport(-1L, 2019);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarterDao.getById(1L));
        QuarterlyReportDTO createdReport = reportManager.createQuarterlyReport(toCreate);
        assertNotNull(createdReport);
        assertNotNull(createdReport.getId());
        assertTrue(createdReport.getId() > 0);
        assertNotNull(createdReport.getAnnualReport());
        assertEquals(createdReport.getAnnualReport().getId().longValue(), annualReport.getId().longValue());
        assertNotNull(createdReport.getAnnualReport().getYear());
        assertEquals(createdReport.getAnnualReport().getYear().intValue(), annualReport.getYear().longValue());
        assertNotNull(createdReport.getAnnualReport().getAcb());
        assertNotNull(createdReport.getAnnualReport().getAcb().getId());
        assertEquals(createdReport.getAnnualReport().getAcb().getId().longValue(), annualReport.getAcb().getId().longValue());
        assertNotNull(createdReport.getQuarter());
        assertEquals(createdReport.getQuarter().getId().longValue(), toCreate.getQuarter().getId().longValue());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAnnualReportDoesNotExist()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Integer year = 2019;
        Long acbId = -2L;

        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(year);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(acbId);
        annualReport.setAcb(acb);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarterDao.getById(1L));
        QuarterlyReportDTO createdReport = reportManager.createQuarterlyReport(toCreate);
        assertNotNull(createdReport);
        assertNotNull(createdReport.getId());
        assertTrue(createdReport.getId() > 0);
        assertNotNull(createdReport.getAnnualReport());
        assertTrue(createdReport.getAnnualReport().getId() > 0);
        assertNotNull(createdReport.getAnnualReport().getYear());
        assertEquals(year.intValue(), createdReport.getAnnualReport().getYear().intValue());
        assertNotNull(createdReport.getAnnualReport().getAcb());
        assertNotNull(createdReport.getAnnualReport().getAcb().getId());
        assertEquals(acbId.longValue(), createdReport.getAnnualReport().getAcb().getId().longValue());
        assertNotNull(createdReport.getQuarter());
        assertEquals(createdReport.getQuarter().getId().longValue(), toCreate.getQuarter().getId().longValue());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportWithoutQuarterId()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String quarterName = "Q1";

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO annualReport = createAnnualReport(-1L, 2019);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName(quarterName);
        toCreate.setQuarter(quarter);
        QuarterlyReportDTO createdReport = reportManager.createQuarterlyReport(toCreate);
        assertNotNull(createdReport);
        assertNotNull(createdReport.getId());
        assertTrue(createdReport.getId() > 0);
        assertNotNull(createdReport.getAnnualReport());
        assertEquals(createdReport.getAnnualReport().getId().longValue(), annualReport.getId().longValue());
        assertNotNull(createdReport.getAnnualReport().getYear());
        assertEquals(createdReport.getAnnualReport().getYear().intValue(), annualReport.getYear().longValue());
        assertNotNull(createdReport.getAnnualReport().getAcb());
        assertNotNull(createdReport.getAnnualReport().getAcb().getId());
        assertEquals(createdReport.getAnnualReport().getAcb().getId().longValue(), annualReport.getAcb().getId().longValue());
        assertNotNull(createdReport.getQuarter());
        assertEquals(createdReport.getQuarter().getName(), quarterName);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsAnonymousUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsAtlUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsOncUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(oncUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsCmsUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(cmsUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportForBadAcbAsAcbUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportForAllowedAcbAsAcbUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        annualReport.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        QuarterlyReportDTO created = reportManager.createQuarterlyReport(toCreate);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangePrioritizedElementSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedPrioritizedElementSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setPrioritizedElementSummary(updatedPrioritizedElementSummary);
        QuarterlyReportDTO updatedReport = reportManager.updateQuarterlyReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getPrioritizedElementSummary());
        assertEquals(updatedPrioritizedElementSummary, updatedReport.getPrioritizedElementSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeReactiveSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedReactiveSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setReactiveSummary(updatedReactiveSummary);
        QuarterlyReportDTO updatedReport = reportManager.updateQuarterlyReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getReactiveSummary());
        assertEquals(updatedReactiveSummary, updatedReport.getReactiveSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeTransparencyDisclosureSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedTransparencyDisclosureSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setTransparencyDisclosureSummary(updatedTransparencyDisclosureSummary);
        QuarterlyReportDTO updatedReport = reportManager.updateQuarterlyReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getTransparencyDisclosureSummary());
        assertEquals(updatedTransparencyDisclosureSummary, updatedReport.getTransparencyDisclosureSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = EntityRetrievalException.class)
    @Rollback(true)
    @Transactional
    public void deleteReport() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO report = createReport();
        reportManager.deleteQuarterlyReport(report.getId());
        reportManager.getQuarterlyReport(report.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getById() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO report = createReport();
        QuarterlyReportDTO fetchedReport = reportManager.getQuarterlyReport(report.getId());
        assertNotNull(fetchedReport);
        assertEquals(report.getId(), fetchedReport.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = EntityRetrievalException.class)
    @Transactional
    public void getByIdDoesNotExist() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        reportManager.getQuarterlyReport(-100L);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getAsAcbHasReport() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO report = createReport();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<QuarterlyReportDTO> acbReports = reportManager.getQuarterlyReports();
        assertNotNull(acbReports);
        assertEquals(1, acbReports.size());
        assertEquals(report.getId(), acbReports.get(0).getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getAsAcbHasNoReports() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<QuarterlyReportDTO> acbReports = reportManager.getQuarterlyReports();
        assertNotNull(acbReports);
        assertEquals(0, acbReports.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void writeQuarterlyReportAsExcelWorkbook()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            IOException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        annualReport.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary("In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. ");
        toCreate.setReactiveSummary("test reactive element summary");
        toCreate.setPrioritizedElementSummary("test prioritized element summary");
        toCreate.setTransparencyDisclosureSummary("test transparency and disclosure summary");
        QuarterlyReportDTO created = reportManager.createQuarterlyReport(toCreate);

        Workbook workbook = reportManager.exportQuarterlyReport(created.getId());
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("test.xlsx");
            workbook.write(outputStream);
        } catch(final Exception ex) {
            fail(ex.getMessage());
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private QuarterlyReportDTO createReport() throws EntityCreationException, EntityRetrievalException {
        return createReport(-1L, 2019, 1L);
    }

    private QuarterlyReportDTO createReport(final Long acbId, final Integer year, final Long quarterId) throws EntityCreationException, EntityRetrievalException {
        QuarterDTO quarter = quarterDao.getById(quarterId);
        AnnualReportDTO annualReport = createAnnualReport(acbId, year);
        String activitiesOutcomesSummary = "summary";
        String prioritizedElementSummary = "test";
        String reactiveSummary = "test";
        String transparencyDisclosureSummary = "test";
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary(activitiesOutcomesSummary);
        toCreate.setPrioritizedElementSummary(prioritizedElementSummary);
        toCreate.setReactiveSummary(reactiveSummary);
        toCreate.setTransparencyDisclosureSummary(transparencyDisclosureSummary);
        QuarterlyReportDTO created = quarterlyReportDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(activitiesOutcomesSummary, created.getActivitiesOutcomesSummary());
        assertEquals(prioritizedElementSummary, created.getPrioritizedElementSummary());
        assertEquals(reactiveSummary, created.getReactiveSummary());
        assertEquals(transparencyDisclosureSummary, created.getTransparencyDisclosureSummary());
        assertNotNull(created.getAnnualReport());
        assertEquals(annualReport.getId(), created.getAnnualReport().getId());
        assertNotNull(created.getQuarter());
        assertEquals(quarter.getId(), created.getQuarter().getId());
        return created;
    }

    private AnnualReportDTO createAnnualReport(Long acbId, Integer year) throws EntityCreationException {
        String findingsSummary = "test";
        String obstacleSummary = "test";
        AnnualReportDTO toCreate = new AnnualReportDTO();
        toCreate.setFindingsSummary(findingsSummary);
        toCreate.setObstacleSummary(obstacleSummary);
        toCreate.setYear(year);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(acbId);
        toCreate.setAcb(acb);
        AnnualReportDTO created = annualReportDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(findingsSummary, created.getFindingsSummary());
        assertEquals(obstacleSummary, created.getObstacleSummary());
        assertEquals(year, created.getYear());
        assertEquals(acb.getId(), created.getAcb().getId());
        return created;
    }
}
