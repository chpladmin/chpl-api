package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.Date;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.builder.AnnualReportBuilderXlsx;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.dao.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.SurveillanceManager;
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
    private SurveillanceManager survManager;

    @Autowired
    private AnnualReportDAO annualReportDao;

    @Autowired
    private SurveillanceDAO survDao;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private AnnualReportBuilderXlsx reportBuilder;

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
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
            JsonProcessingException {
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
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
            JsonProcessingException {
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
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            JsonProcessingException {
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
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            JsonProcessingException {
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
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            JsonProcessingException {
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
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            JsonProcessingException {
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
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            JsonProcessingException {
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
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            JsonProcessingException {
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
    public void updateReportChangeFindingsSummary() throws EntityCreationException, EntityRetrievalException,
        JsonProcessingException {
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
    public void updateReportChangeReactiveSummary() throws EntityCreationException, EntityRetrievalException,
        JsonProcessingException {
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
    public void deleteReport() throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
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
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);

        //create some quarterly reports to go with the annual
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary("In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. ");
        toCreate.setReactiveSummary("test reactive element summary");
        toCreate.setPrioritizedElementSummary("test prioritized element summary for Q1");
        toCreate.setTransparencyDisclosureSummary("test transparency and disclosure summary for Q1");
        QuarterlyReportDTO q1Report = reportManager.createQuarterlyReport(toCreate);

        //create a surveillance to add to the report
        //surv will start one day after the quarter begins
        Surveillance createdSurv1 = createSurveillance(1L, new Date(q1Report.getStartDate().getTime() + (24*60*60*1000)));
        createSurveillance(2L, new Date(q1Report.getStartDate().getTime() + (48*60*60*1000)));
        createSurveillance(3L, new Date(q1Report.getStartDate().getTime() + (72*60*60*1000)));

        PrivilegedSurveillanceDTO privilegedSurvData = new PrivilegedSurveillanceDTO();
        privilegedSurvData.setId(createdSurv1.getId());
        privilegedSurvData.setQuarterlyReport(q1Report);
        privilegedSurvData.setK1Reviewed(true);
        privilegedSurvData.setAdditionalCostsEvaluation("Some additional costs");
        privilegedSurvData.setDirectionDeveloperResolution("direction!");
        reportManager.createOrUpdateQuarterlyReportSurveillanceMap(privilegedSurvData);

        //add excluded listing to the quarter
        reportManager.createQuarterlyReportExclusion(q1Report, 1L, "A really good reason for q1");
        reportManager.createQuarterlyReportExclusion(q1Report, 3L, "A really good reason for listing id 3");

        quarter = new QuarterDTO();
        quarter.setName("Q2");
        toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary("Shorter summary for Q2. ");
        toCreate.setReactiveSummary("test reactive element summary for Q2");
        toCreate.setPrioritizedElementSummary("test prioritized element summary for Q2");
        toCreate.setTransparencyDisclosureSummary("test transparency and disclosure summary for Q2");
        QuarterlyReportDTO q2Report = reportManager.createQuarterlyReport(toCreate);

        privilegedSurvData = new PrivilegedSurveillanceDTO();
        privilegedSurvData.setId(createdSurv1.getId());
        privilegedSurvData.setQuarterlyReport(q2Report);
        privilegedSurvData.setK1Reviewed(false);
        privilegedSurvData.setAdditionalCostsEvaluation("Some additional costs");
        privilegedSurvData.setDirectionDeveloperResolution("a different developer resolution");
        privilegedSurvData.setCompletedCapVerification("did it");
        reportManager.createOrUpdateQuarterlyReportSurveillanceMap(privilegedSurvData);

        //add excluded listing to the quarter
        //exclusion was copied from q1 report; get it and update the reason
        QuarterlyReportExclusionDTO exclusion = reportManager.getExclusion(q2Report, 1L);
        assertNotNull(exclusion);
        reportManager.updateQuarterlyReportExclusion(q2Report, 1L, "A really good reason for q2");

        quarter = new QuarterDTO();
        quarter.setName("Q3");
        toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary("Shorter summary for Q3.");
        toCreate.setReactiveSummary("test reactive element summary for Q3");
        toCreate.setPrioritizedElementSummary("test prioritized element summary for Q3");
        toCreate.setTransparencyDisclosureSummary("test transparency and disclosure summary for Q");
        QuarterlyReportDTO q3Report = reportManager.createQuarterlyReport(toCreate);

        privilegedSurvData = new PrivilegedSurveillanceDTO();
        privilegedSurvData.setId(createdSurv1.getId());
        privilegedSurvData.setQuarterlyReport(q3Report);
        privilegedSurvData.setK1Reviewed(true);
        privilegedSurvData.setAdditionalCostsEvaluation("Some additional costs");
        privilegedSurvData.setDirectionDeveloperResolution("a q3 developer resolution");
        privilegedSurvData.setCompletedCapVerification("did it");
        reportManager.createOrUpdateQuarterlyReportSurveillanceMap(privilegedSurvData);

        quarter = new QuarterDTO();
        quarter.setName("Q4");
        toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary("Shorter summary for Q4.");
        toCreate.setReactiveSummary("test reactive element summary for Q4");
        toCreate.setPrioritizedElementSummary("test prioritized element summary for Q4");
        toCreate.setTransparencyDisclosureSummary("test transparency and disclosure summary for Q4");
        QuarterlyReportDTO q4Report = reportManager.createQuarterlyReport(toCreate);

        privilegedSurvData = new PrivilegedSurveillanceDTO();
        privilegedSurvData.setId(createdSurv1.getId());
        privilegedSurvData.setQuarterlyReport(q4Report);
        privilegedSurvData.setK1Reviewed(true);
        privilegedSurvData.setAdditionalCostsEvaluation("q4 additional costs");
        privilegedSurvData.setDirectionDeveloperResolution("a q4 developer resolution");
        privilegedSurvData.setCompletedCapVerification("did it");
        reportManager.createOrUpdateQuarterlyReportSurveillanceMap(privilegedSurvData);

        //create the annual report
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        annualReport.setAcb(acb);
        annualReport.setObstacleSummary("Necessary ye contented newspaper zealously breakfast he prevailed. Melancholy middletons yet understood decisively boy law she. Answer him easily are its barton little. Oh no though mother be things simple itself. Dashwood horrible he strictly on as. Home fine in so am good body this hope. \n" +
                "\n" +
                "Lose eyes get fat shew. Winter can indeed letter oppose way change tended now. So is improve my charmed picture exposed adapted demands. Received had end produced prepared diverted strictly off man branched. Known ye money so large decay voice there to. Preserved be mr cordially incommode as an. He doors quick child an point at. Had share vexed front least style off why him. \n" +
                "\n" +
                "Scarcely on striking packages by so property in delicate. Up or well must less rent read walk so be. Easy sold at do hour sing spot. Any meant has cease too the decay. Since party burst am it match. By or blushes between besides offices noisier as. Sending do brought winding compass in. Paid day till shed only fact age its end. \n" +
                "Necessary ye contented newspaper zealously breakfast he prevailed. Melancholy middletons yet understood decisively boy law she. Answer him easily are its barton little. Oh no though mother be things simple itself. Dashwood horrible he strictly on as. Home fine in so am good body this hope. \n" +
                "\n" +
                "Lose eyes get fat shew. Winter can indeed letter oppose way change tended now. So is improve my charmed picture exposed adapted demands. Received had end produced prepared diverted strictly off man branched. Known ye money so large decay voice there to. Preserved be mr cordially incommode as an. He doors quick child an point at. Had share vexed front least style off why him. \n" +
                "\n");
        annualReport.setFindingsSummary("Behind sooner dining so window excuse he summer. Breakfast met certainty and fulfilled propriety led. Waited get either are wooded little her. Contrasted unreserved as mr particular collecting it everything as indulgence. Seems ask meant merry could put. Age old begin had boy noisy table front whole given. \n" +
                "\n" +
                "Barton did feebly change man she afford square add. Want eyes by neat so just must. Past draw tall up face show rent oh mr. Required is debating extended wondered as do. New get described applauded incommode shameless out extremity but. Resembled at perpetual no believing is otherwise sportsman. Is do he dispatched cultivated travelling astonished. Melancholy am considered possession on collecting everything. \n");
        AnnualReportDTO created = reportManager.createAnnualReport(annualReport);

        AnnualReportDTO fetchedReport = reportManager.getAnnualReport(created.getId());
        Workbook workbook = reportBuilder.buildXlsx(fetchedReport);
        assertNotNull(workbook);

        //uncomment to write report
//        OutputStream outputStream = null;
//        try {
//            outputStream = new FileOutputStream("annual.xlsx");
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
            UserRetrievalException, IOException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        AnnualReportDTO annualReport = new AnnualReportDTO();
        annualReport.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-4L);
        annualReport.setAcb(acb);
        annualReport.setFindingsSummary("test findings summary");
        annualReport.setObstacleSummary("test obstacle summary");
        AnnualReportDTO created = reportManager.createAnnualReport(annualReport);

        reportManager.exportAnnualReportAsBackgroundJob(created.getId());
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

    private Surveillance createSurveillance(final Long listingId, final Date startDate) throws EntityRetrievalException {
        Surveillance surv = new Surveillance();

        CertifiedProductDTO cpDto = cpDao.getById(listingId);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(startDate);
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
            insertedId = survManager.createSurveillance(surv);
            assertNotNull(insertedId);
            surv = survManager.getById(insertedId);
            assertNotNull(surv);
            assertNotNull(surv.getCertifiedProduct());
            assertEquals(cpDto.getId(), surv.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), surv.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), surv.getRandomizedSitesUsed());
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
        return surv;
    }
}
