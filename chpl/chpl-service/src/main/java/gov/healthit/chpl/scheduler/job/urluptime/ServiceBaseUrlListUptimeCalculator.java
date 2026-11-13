package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.manager.CertificationBodyManager;

@Component
public class ServiceBaseUrlListUptimeCalculator {
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;
    private Logger logger;

    private List<CertificationBody> activeAcbs;

    @Autowired
    public ServiceBaseUrlListUptimeCalculator(UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO, CertificationBodyManager certificationBodyManager) {
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;

        activeAcbs = certificationBodyManager.getAllActive();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Transactional(readOnly =  true)
    public List<ServiceBaseUrlListUptimeReport> calculateRowsForReport() {
        LocalDateTime end = LocalDateTime.now().minusDays(1).with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
        LocalDateTime currentMonthStart = end.withDayOfMonth(1).with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());
        LocalDateTime pastWeekStart = LocalDateTime.now().minusWeeks(1).with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());

        Map<Long, UrlUptimeMonitorTestCounts> countsByMonitorId
                = urlUptimeMonitorTestDAO.getTestCountsByMonitor(currentMonthStart, pastWeekStart, end);

        return urlUptimeMonitorDAO.getAll().stream()
                .map(monitor -> buildUptimeReportForMonitor(monitor, countsByMonitorId.getOrDefault(monitor.getId(),
                        UrlUptimeMonitorTestCounts.builder().urlUptimeMonitorId(monitor.getId()).build())))
                .collect(Collectors.toList());
    }

    private ServiceBaseUrlListUptimeReport buildUptimeReportForMonitor(UrlUptimeMonitor urlUptimeMonitor, UrlUptimeMonitorTestCounts counts) {
        logger.info("Summarizing data for {}", urlUptimeMonitor.getUrl());

        return ServiceBaseUrlListUptimeReport.builder()
                .developerName(urlUptimeMonitor.getDeveloper().getName())
                .developerId(urlUptimeMonitor.getDeveloper().getId())
                .url(urlUptimeMonitor.getUrl())
                .totalTestCount(counts.getTotalTestCount())
                .totalSuccessfulTestCount(counts.getTotalSuccessfulTestCount())
                .currentMonthTestCount(counts.getCurrentMonthTestCount())
                .currentMonthSuccessfulTestCount(counts.getCurrentMonthSuccessfulTestCount())
                .pastWeekTestCount(counts.getPastWeekTestCount())
                .pastWeekSuccessfulTestCount(counts.getPastWeekSuccessfulTestCount())
                .applicableAcbsMap(getApplicableActiveAcbsMap(urlUptimeMonitor))
                .build();
    }

    private Map<Long, Boolean> getApplicableActiveAcbsMap(UrlUptimeMonitor urlUptimeMonitor) {
        List<Long> acbIds = Arrays.asList(urlUptimeMonitor.getDelimitedAcbIds()
                .split(","))
                .stream()
                .map(id -> Long.valueOf(id))
                .toList();

        return activeAcbs.stream()
                .collect(Collectors.toMap(
                        acb -> acb.getId(),
                        acb -> acbIds.stream()
                        .filter(acbId -> acbId.equals(acb.getId()))
                        .findAny()
                        .isPresent()));
    }
}
