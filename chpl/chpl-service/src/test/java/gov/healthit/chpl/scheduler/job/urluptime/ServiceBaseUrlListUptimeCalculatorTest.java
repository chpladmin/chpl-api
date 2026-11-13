package gov.healthit.chpl.scheduler.job.urluptime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.manager.CertificationBodyManager;

public class ServiceBaseUrlListUptimeCalculatorTest {
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;
    private ServiceBaseUrlListUptimeCalculator calculator;

    @BeforeEach
    public void setup() {
        urlUptimeMonitorDAO = Mockito.mock(UrlUptimeMonitorDAO.class);
        urlUptimeMonitorTestDAO = Mockito.mock(UrlUptimeMonitorTestDAO.class);
        CertificationBodyManager certificationBodyManager = Mockito.mock(CertificationBodyManager.class);
        Mockito.when(certificationBodyManager.getAllActive()).thenReturn(Stream.of(
                CertificationBody.builder().id(1L).name("ACB 1").build(),
                CertificationBody.builder().id(2L).name("ACB 2").build())
                .collect(Collectors.toList()));

        calculator = new ServiceBaseUrlListUptimeCalculator(urlUptimeMonitorDAO, urlUptimeMonitorTestDAO, certificationBodyManager);
        calculator.setLogger(LogManager.getLogger(ServiceBaseUrlListUptimeCalculatorTest.class));
    }

    @Test
    public void calculateRowsForReport_noMonitors_returnsEmptyList() {
        Mockito.when(urlUptimeMonitorDAO.getAll()).thenReturn(new ArrayList<UrlUptimeMonitor>());
        Mockito.when(urlUptimeMonitorTestDAO.getTestCountsByMonitor(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(new HashMap<Long, UrlUptimeMonitorTestCounts>());

        List<ServiceBaseUrlListUptimeReport> rows = calculator.calculateRowsForReport();

        assertEquals(0, rows.size());
    }

    @Test
    public void calculateRowsForReport_monitorHasCounts_copiesCountsToReport() {
        Mockito.when(urlUptimeMonitorDAO.getAll()).thenReturn(Stream.of(getMonitor(10L, "https://a.com", "1"))
                .collect(Collectors.toList()));
        Map<Long, UrlUptimeMonitorTestCounts> counts = new HashMap<Long, UrlUptimeMonitorTestCounts>();
        counts.put(10L, UrlUptimeMonitorTestCounts.builder()
                .urlUptimeMonitorId(10L)
                .totalTestCount(100L)
                .totalSuccessfulTestCount(90L)
                .currentMonthTestCount(20L)
                .currentMonthSuccessfulTestCount(18L)
                .pastWeekTestCount(7L)
                .pastWeekSuccessfulTestCount(6L)
                .build());
        Mockito.when(urlUptimeMonitorTestDAO.getTestCountsByMonitor(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(counts);

        List<ServiceBaseUrlListUptimeReport> rows = calculator.calculateRowsForReport();

        assertEquals(1, rows.size());
        ServiceBaseUrlListUptimeReport row = rows.get(0);
        assertEquals("https://a.com", row.getUrl());
        assertEquals(5L, row.getDeveloperId().longValue());
        assertEquals("Dev 5", row.getDeveloperName());
        assertEquals(100L, row.getTotalTestCount().longValue());
        assertEquals(90L, row.getTotalSuccessfulTestCount().longValue());
        assertEquals(20L, row.getCurrentMonthTestCount().longValue());
        assertEquals(18L, row.getCurrentMonthSuccessfulTestCount().longValue());
        assertEquals(7L, row.getPastWeekTestCount().longValue());
        assertEquals(6L, row.getPastWeekSuccessfulTestCount().longValue());
    }

    @Test
    public void calculateRowsForReport_monitorHasNoTests_returnsZeroCounts() {
        Mockito.when(urlUptimeMonitorDAO.getAll()).thenReturn(Stream.of(getMonitor(10L, "https://a.com", "1"))
                .collect(Collectors.toList()));
        Mockito.when(urlUptimeMonitorTestDAO.getTestCountsByMonitor(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(new HashMap<Long, UrlUptimeMonitorTestCounts>());

        List<ServiceBaseUrlListUptimeReport> rows = calculator.calculateRowsForReport();

        assertEquals(1, rows.size());
        ServiceBaseUrlListUptimeReport row = rows.get(0);
        assertEquals(0L, row.getTotalTestCount().longValue());
        assertEquals(0L, row.getTotalSuccessfulTestCount().longValue());
        assertEquals(0L, row.getCurrentMonthTestCount().longValue());
        assertEquals(0L, row.getCurrentMonthSuccessfulTestCount().longValue());
        assertEquals(0L, row.getPastWeekTestCount().longValue());
        assertEquals(0L, row.getPastWeekSuccessfulTestCount().longValue());
    }

    @Test
    public void calculateRowsForReport_multipleMonitors_matchesCountsToCorrectMonitor() {
        Mockito.when(urlUptimeMonitorDAO.getAll()).thenReturn(Stream.of(
                getMonitor(10L, "https://a.com", "1"),
                getMonitor(11L, "https://b.com", "2"))
                .collect(Collectors.toList()));
        Map<Long, UrlUptimeMonitorTestCounts> counts = new HashMap<Long, UrlUptimeMonitorTestCounts>();
        counts.put(10L, UrlUptimeMonitorTestCounts.builder().urlUptimeMonitorId(10L).totalTestCount(5L).build());
        counts.put(11L, UrlUptimeMonitorTestCounts.builder().urlUptimeMonitorId(11L).totalTestCount(8L).build());
        Mockito.when(urlUptimeMonitorTestDAO.getTestCountsByMonitor(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(counts);

        List<ServiceBaseUrlListUptimeReport> rows = calculator.calculateRowsForReport();

        assertEquals(2, rows.size());
        assertEquals("https://a.com", rows.get(0).getUrl());
        assertEquals(5L, rows.get(0).getTotalTestCount().longValue());
        assertEquals("https://b.com", rows.get(1).getUrl());
        assertEquals(8L, rows.get(1).getTotalTestCount().longValue());
    }

    @Test
    public void calculateRowsForReport_monitorHasAcbs_setsApplicableAcbsMap() {
        Mockito.when(urlUptimeMonitorDAO.getAll()).thenReturn(Stream.of(getMonitor(10L, "https://a.com", "2"))
                .collect(Collectors.toList()));
        Mockito.when(urlUptimeMonitorTestDAO.getTestCountsByMonitor(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(new HashMap<Long, UrlUptimeMonitorTestCounts>());

        List<ServiceBaseUrlListUptimeReport> rows = calculator.calculateRowsForReport();

        Map<Long, Boolean> acbsMap = rows.get(0).getApplicableAcbsMap();
        assertEquals(2, acbsMap.size());
        assertFalse(acbsMap.get(1L));
        assertTrue(acbsMap.get(2L));
    }

    @Test
    public void calculateRowsForReport_queriesCounts_passesExpectedDateBoundaries() {
        Mockito.when(urlUptimeMonitorDAO.getAll()).thenReturn(new ArrayList<UrlUptimeMonitor>());
        Mockito.when(urlUptimeMonitorTestDAO.getTestCountsByMonitor(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(new HashMap<Long, UrlUptimeMonitorTestCounts>());

        calculator.calculateRowsForReport();

        ArgumentCaptor<LocalDateTime> monthStart = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> weekStart = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        Mockito.verify(urlUptimeMonitorTestDAO, Mockito.times(1))
                .getTestCountsByMonitor(monthStart.capture(), weekStart.capture(), end.capture());

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        assertEquals(yesterday.toLocalDate(), end.getValue().toLocalDate());
        assertEquals(23, end.getValue().getHour());
        assertEquals(yesterday.toLocalDate().withDayOfMonth(1), monthStart.getValue().toLocalDate());
        assertEquals(0, monthStart.getValue().getHour());
        assertEquals(LocalDateTime.now().minusWeeks(1).toLocalDate(), weekStart.getValue().toLocalDate());
        assertEquals(0, weekStart.getValue().getHour());
    }

    private UrlUptimeMonitor getMonitor(Long id, String url, String delimitedAcbIds) {
        return UrlUptimeMonitor.builder()
                .id(id)
                .url(url)
                .delimitedAcbIds(delimitedAcbIds)
                .developer(Developer.builder()
                        .id(5L)
                        .name("Dev 5")
                        .build())
                .build();
    }
}
