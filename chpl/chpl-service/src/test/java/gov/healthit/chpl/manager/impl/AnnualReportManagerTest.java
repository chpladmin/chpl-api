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
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
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
public class AnnualReportManagerTest extends TestCase {

    private static JWTAuthenticatedUser adminUser, oncUser, acbUser, atlUser, cmsUser;

    @Autowired
    private SurveillanceReportManager reportManager;

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

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createAnnualReportMissingAcb()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        reportManager.createAnnualReport(annualReport);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createAnnualReportMissingYear()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        reportManager.createAnnualReport(annualReport);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    @Rollback(true)
    @Transactional
    public void createAnnualReportAsAnonymousUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        reportManager.createAnnualReport(annualReport);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createAnnualReportAsAtlUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        reportManager.createAnnualReport(annualReport);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createAnnualReportAsOncUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(oncUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        reportManager.createAnnualReport(annualReport);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createAnnualReportAsCmsUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(cmsUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        reportManager.createAnnualReport(annualReport);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createAnnualReportForBadAcbAsAcbUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        annualReport.setAcb(acb);
        reportManager.createAnnualReport(annualReport);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createAnnualReportForAllowedAcbAsAcbUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        annualReport.setAcb(acb);
        reportManager.createAnnualReport(annualReport);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeFindingsSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedFindingsSummary = "new summary";
        AnnualReportDTO report = createReport();
        report.setFindingsSummary(updatedFindingsSummary);
        AnnualReportDTO updatedReport = reportManager.updateAnnualReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getFindingsSummary());
        assertEquals(updatedFindingsSummary, updatedReport.getFindingsSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeReactiveSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedObstacleSummary = "new summary";
        AnnualReportDTO report = createReport();
        report.setObstacleSummary(updatedObstacleSummary);
        AnnualReportDTO updatedReport = reportManager.updateAnnualReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getObstacleSummary());
        assertEquals(updatedObstacleSummary, updatedReport.getObstacleSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = EntityRetrievalException.class)
    @Rollback(true)
    @Transactional
    public void deleteReport() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO report = createReport();
        reportManager.deleteAnnualReport(report.getId());
        reportManager.getAnnualReport(report.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getById() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO report = createReport();
        AnnualReportDTO fetchedReport = reportManager.getAnnualReport(report.getId());
        assertNotNull(fetchedReport);
        assertEquals(report.getId(), fetchedReport.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = EntityRetrievalException.class)
    @Transactional
    public void getByIdDoesNotExist() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        reportManager.getAnnualReport(-100L);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getAsAcbHasReport() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        AnnualReportDTO report = createReport();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<AnnualReportDTO> acbReports = reportManager.getAnnualReports();
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
        List<AnnualReportDTO> acbReports = reportManager.getAnnualReports();
        assertNotNull(acbReports);
        assertEquals(0, acbReports.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void writeAnnualReportAsExcelWorkbook()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            IOException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        annualReport.setAcb(acb);
        annualReport.setFindingsSummary("test findings summary");
        annualReport.setObstacleSummary("test obstacle summary");
        AnnualReportDTO created = reportManager.createAnnualReport(annualReport);

        Workbook workbook = reportManager.exportAnnualReport(created.getId());
        assertNotNull(workbook);

        //uncomment to write report
//        OutputStream outputStream = null;
//        try {
//            outputStream = new FileOutputStream("test-annual.xlsx");
//            workbook.write(outputStream);
//        } catch(final Exception ex) {
//            fail(ex.getMessage());
//        } finally {
//            outputStream.flush();
//            outputStream.close();
//        }

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void writeAnnualReportAsExcelWorkbook_AcbNotAllowed()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            IOException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-4L);
        annualReport.setAcb(acb);
        annualReport.setFindingsSummary("test findings summary");
        annualReport.setObstacleSummary("test obstacle summary");
        AnnualReportDTO created = reportManager.createAnnualReport(annualReport);

        reportManager.exportAnnualReport(created.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private AnnualReportDTO createReport() throws EntityCreationException, EntityRetrievalException {
        return createReport(-1L, 2019);
    }

    private AnnualReportDTO createReport(final Long acbId, final Integer year) throws EntityCreationException {
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
