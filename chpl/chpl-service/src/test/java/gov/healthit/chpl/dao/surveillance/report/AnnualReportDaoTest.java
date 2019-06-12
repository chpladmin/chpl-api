package gov.healthit.chpl.dao.surveillance.report;

import static org.junit.Assert.assertNotEquals;

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
public class AnnualReportDaoTest extends TestCase {

    @Autowired
    private AnnualReportDAO annualReportDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Rollback(true)
    @Transactional
    public void createReportTest() throws EntityCreationException {
        createReport();
    }

    @Test(expected = EntityCreationException.class)
    @Rollback(true)
    @Transactional
    public void createReportMissingAcb() throws EntityCreationException {
        AnnualReportDTO toCreate = new AnnualReportDTO();
        annualReportDao.create(toCreate);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeFindingsSummary() throws EntityCreationException, EntityRetrievalException {
        String updatedFindingsSummary = "a new findings summary";
        AnnualReportDTO report = createReport();
        report.setFindingsSummary(updatedFindingsSummary);
        AnnualReportDTO updatedReport = annualReportDao.update(report);
        assertNotNull(updatedReport);
        assertEquals(report.getId(), updatedReport.getId());
        assertEquals(updatedFindingsSummary, updatedReport.getFindingsSummary());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeObstacleSummary() throws EntityCreationException, EntityRetrievalException {
        String updatedObstacleSummary = "a new obstacle summary";
        AnnualReportDTO report = createReport();
        report.setObstacleSummary(updatedObstacleSummary);
        AnnualReportDTO updatedReport = annualReportDao.update(report);
        assertNotNull(updatedReport);
        assertEquals(report.getId(), updatedReport.getId());
        assertEquals(updatedObstacleSummary, updatedReport.getObstacleSummary());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeYear() throws EntityCreationException, EntityRetrievalException {
        Integer updatedYear = 2020;
        AnnualReportDTO report = createReport();
        report.setYear(updatedYear);
        AnnualReportDTO updatedReport = annualReportDao.update(report);
        assertNotNull(updatedReport);
        assertEquals(report.getId(), updatedReport.getId());
        assertEquals(updatedYear, updatedReport.getYear());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportCannotChangeAcb() throws EntityCreationException, EntityRetrievalException {
        CertificationBodyDTO updatedAcb = new CertificationBodyDTO();
        updatedAcb.setId(-2L);
        AnnualReportDTO report = createReport();
        report.setAcb(updatedAcb);
        AnnualReportDTO updatedReport = annualReportDao.update(report);
        assertNotNull(updatedReport);
        assertEquals(report.getId(), updatedReport.getId());
        assertNotNull(updatedReport.getAcb());
        assertNotEquals(updatedAcb.getId(), updatedReport.getAcb().getId());
    }

    @Test(expected = EntityRetrievalException.class)
    @Rollback(true)
    @Transactional
    public void deleteReport() throws EntityCreationException, EntityRetrievalException {
        AnnualReportDTO report = createReport();
        annualReportDao.delete(report.getId());
        annualReportDao.getById(report.getId());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getByAcb() throws EntityCreationException, EntityRetrievalException {
        AnnualReportDTO report = createReport();
        List<AnnualReportDTO> reports = annualReportDao.getByAcb(report.getAcb().getId());
        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals(report.getId(), reports.get(0).getId());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getByAcbAndYear() throws EntityCreationException, EntityRetrievalException {
        AnnualReportDTO report = createReport();
        AnnualReportDTO foundReport = annualReportDao.getByAcbAndYear(report.getAcb().getId(), report.getYear());
        assertNotNull(foundReport);
        assertEquals(report.getId(), foundReport.getId());
    }

    private AnnualReportDTO createReport() throws EntityCreationException {
        String findingsSummary = "test";
        String obstacleSummary = "test";
        Integer year = 2019;
        AnnualReportDTO toCreate = new AnnualReportDTO();
        toCreate.setFindingsSummary(findingsSummary);
        toCreate.setObstacleSummary(obstacleSummary);
        toCreate.setYear(year);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
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
