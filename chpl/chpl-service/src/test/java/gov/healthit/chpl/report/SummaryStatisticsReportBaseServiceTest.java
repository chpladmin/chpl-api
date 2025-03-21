package gov.healthit.chpl.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

public class SummaryStatisticsReportBaseServiceTest {

    @Mock
    private SummaryStatisticsDAO summaryStatisticsDAO;

    @Mock
    private CertificationBodyManager certificationBodyManager;

    @InjectMocks
    private SummaryStatisticsReportBaseService summaryStatisticsReportBaseService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    public void testGetStatistics_Success() throws Exception {
//        SummaryStatisticsEntity summaryStatisticsEntity = mock(SummaryStatisticsEntity.class);
//        when(summaryStatisticsDAO.getCurrentSummaryStatistics()).thenReturn(summaryStatisticsEntity);
//        when(summaryStatisticsEntity.getSummaryStatistics()).thenReturn("{\"someField\":\"someValue\"}");
//
//        StatisticsSnapshot result = summaryStatisticsReportBaseService.getStatistics();
//
//        assertNotNull(result);
//        assertEquals("someValue", result.getSomeField());
//    }

    @Test
    public void testGetStatistics_Exception() throws Exception {
        when(summaryStatisticsDAO.getCurrentSummaryStatistics()).thenThrow(new RuntimeException("Database error"));

        StatisticsSnapshot result = summaryStatisticsReportBaseService.getStatistics();

        assertNull(result);
    }

    @Test
    public void testGetGeneratedAcbName_Success() throws EntityRetrievalException {
        CertificationBody certificationBody = mock(CertificationBody.class);
        when(certificationBodyManager.getById(1L)).thenReturn(certificationBody);
        when(certificationBody.getName()).thenReturn("ACB Name");
        when(certificationBody.isRetired()).thenReturn(true);

        String result = summaryStatisticsReportBaseService.getGeneratedAcbName(1L);

        assertEquals("ACB Name (Retired)", result);
    }

    @Test()
    public void testGetGeneratedAcbName_ReturnsUnknown() throws EntityRetrievalException {
        when(certificationBodyManager.getById(1L)).thenThrow(new EntityRetrievalException("Not found"));

        String result = summaryStatisticsReportBaseService.getGeneratedAcbName(1L);

        assertEquals("Unknown", result);
    }

    @Test
    public void testUpdateAcbNameBasedOnRetired_Success() throws EntityRetrievalException {
        CertificationBody certificationBody = mock(CertificationBody.class);
        IdNamePair acb = IdNamePair.builder().id(1L).name("ACB Name").build();
        when(certificationBodyManager.getById(1L)).thenReturn(certificationBody);
        when(certificationBody.isRetired()).thenReturn(true);


        IdNamePair result = summaryStatisticsReportBaseService.updateAcbNameBasedOnRetired(acb);

        assertEquals("ACB Name (Retired)", result.getName());
    }
}