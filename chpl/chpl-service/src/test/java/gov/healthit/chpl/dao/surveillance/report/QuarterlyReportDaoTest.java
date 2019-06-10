package gov.healthit.chpl.dao.surveillance.report;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
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
public class QuarterlyReportDaoTest extends TestCase {

    @Autowired
    private QuarterlyReportDAO quarterlyReportDao;

    @Autowired
    private QuarterDAO quarterDao;

    @Autowired
    private AnnualReportDAO annualReportDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Rollback(true)
    @Transactional
    public void createReportTest() throws EntityCreationException, EntityRetrievalException {
        createReport();
    }

    @Test(expected = EntityCreationException.class)
    @Rollback(true)
    @Transactional
    public void createReportMissingAnnualReport() throws EntityCreationException, EntityRetrievalException {
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        QuarterDTO quarter = quarterDao.getById(1L);
        toCreate.setQuarter(quarter);
        quarterlyReportDao.create(toCreate);
    }

    @Test(expected = EntityCreationException.class)
    @Rollback(true)
    @Transactional
    public void createReportMissingQuarter() throws EntityCreationException {
        AnnualReportDTO annualReport = createAnnualReport(-1L, 2019);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        quarterlyReportDao.create(toCreate);
    }


    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangePrioritizedElementSummary() throws EntityCreationException, EntityRetrievalException {
        String updatedPrioritizedElementSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setPrioritizedElementSummary(updatedPrioritizedElementSummary);
        QuarterlyReportDTO updatedReport = quarterlyReportDao.update(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getPrioritizedElementSummary());
        assertEquals(updatedPrioritizedElementSummary, updatedReport.getPrioritizedElementSummary());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeReactiveSummary() throws EntityCreationException, EntityRetrievalException {
        String updatedReactiveSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setReactiveSummary(updatedReactiveSummary);
        QuarterlyReportDTO updatedReport = quarterlyReportDao.update(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getReactiveSummary());
        assertEquals(updatedReactiveSummary, updatedReport.getReactiveSummary());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeTransparencyDisclosureSummary() throws EntityCreationException, EntityRetrievalException {
        String updatedTransparencyDisclosureSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setTransparencyDisclosureSummary(updatedTransparencyDisclosureSummary);
        QuarterlyReportDTO updatedReport = quarterlyReportDao.update(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getTransparencyDisclosureSummary());
        assertEquals(updatedTransparencyDisclosureSummary, updatedReport.getTransparencyDisclosureSummary());
    }

    @Test(expected = EntityRetrievalException.class)
    @Rollback(true)
    @Transactional
    public void deleteReport() throws EntityCreationException, EntityRetrievalException {
        QuarterlyReportDTO report = createReport();
        quarterlyReportDao.delete(report.getId());
        quarterlyReportDao.getById(report.getId());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getById() throws EntityCreationException, EntityRetrievalException {
        QuarterlyReportDTO report = createReport();
        QuarterlyReportDTO fetchedReport = quarterlyReportDao.getById(report.getId());
        assertNotNull(fetchedReport);
        assertEquals(report.getId(), fetchedReport.getId());
    }

    @Test(expected = EntityRetrievalException.class)
    @Transactional
    public void getByIdDoesNotExist() throws EntityCreationException, EntityRetrievalException {
        quarterlyReportDao.getById(-100L);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getByAcbHasReport() throws EntityCreationException, EntityRetrievalException {
        QuarterlyReportDTO report = createReport();
        List<QuarterlyReportDTO> acbReports = quarterlyReportDao.getByAcb(-1L);
        assertNotNull(acbReports);
        assertEquals(1, acbReports.size());
        assertEquals(report.getId(), acbReports.get(0).getId());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getByAcbHasNoReports() throws EntityCreationException, EntityRetrievalException {
        List<QuarterlyReportDTO> acbReports = quarterlyReportDao.getByAcb(-1L);
        assertNotNull(acbReports);
        assertEquals(0, acbReports.size());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getByAcbAndYearHasReport() throws EntityCreationException, EntityRetrievalException {
        QuarterlyReportDTO report = createReport(-1L, 2019, 1L);
        List<QuarterlyReportDTO> foundReports = quarterlyReportDao.getByAcbAndYear(-1L, 2019);
        assertNotNull(foundReports);
        assertEquals(1, foundReports.size());
        assertEquals(report.getId(), foundReports.get(0).getId());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getByAcbAndYearHasMultipleReports() throws EntityCreationException, EntityRetrievalException {
        QuarterlyReportDTO q1Report = createReport(-1L, 2019, 1L);
        QuarterlyReportDTO q2Report = createReport(-1L, 2019, 2L);
        List<QuarterlyReportDTO> foundReports = quarterlyReportDao.getByAcbAndYear(-1L, 2019);
        assertNotNull(foundReports);
        assertEquals(2, foundReports.size());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getByAcbAndYearHasNoReports() throws EntityCreationException, EntityRetrievalException {
        createReport(-1L, 2019, 1L);
        List<QuarterlyReportDTO> foundReports = quarterlyReportDao.getByAcbAndYear(-1L, 2018);
        assertNotNull(foundReports);
        assertEquals(0, foundReports.size());
    }

    private QuarterlyReportDTO createReport() throws EntityCreationException, EntityRetrievalException {
        return createReport(-1L, 2019, 1L);
    }

    private QuarterlyReportDTO createReport(final Long acbId, final Integer year, final Long quarterId) throws EntityCreationException, EntityRetrievalException {
        QuarterDTO quarter = quarterDao.getById(quarterId);
        AnnualReportDTO annualReport = createAnnualReport(acbId, year);
        String activitiesSummary = "test";
        String prioritizedElementSummary = "test";
        String reactiveSummary = "test";
        String transparencyDisclosureSummary = "test";
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setAnnualReport(annualReport);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary(activitiesSummary);
        toCreate.setPrioritizedElementSummary(prioritizedElementSummary);
        toCreate.setReactiveSummary(reactiveSummary);
        toCreate.setTransparencyDisclosureSummary(transparencyDisclosureSummary);
        QuarterlyReportDTO created = quarterlyReportDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(activitiesSummary, created.getActivitiesOutcomesSummary());
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
