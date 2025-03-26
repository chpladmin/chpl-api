package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.manager.CertificationBodyManager;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBaseUrlListUptimeEmailJobLogger")
@Component
public class ServiceBaseUrlListUptimeCalculator {
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;

    private List<CertificationBody> activeAcbs;

    @Autowired
    public ServiceBaseUrlListUptimeCalculator(UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO, CertificationBodyManager certificationBodyManager) {
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;

        activeAcbs = certificationBodyManager.getAllActive();
    }

    @Transactional(readOnly =  true)
    public List<ServiceBaseUrlListUptimeReport> calculateRowsForReport() {
        List<ServiceBaseUrlListUptimeReport> reports = new ArrayList<ServiceBaseUrlListUptimeReport>();

        getChplUptimeMonitors().stream()
                .forEach(monitor -> reports.add(summarize(monitor, getUrlUptimeMonitorTests(monitor.getId()))));
        return reports;
    }

    private List<UrlUptimeMonitor> getChplUptimeMonitors() {
        return urlUptimeMonitorDAO.getAll();
    }

    private List<UrlUptimeMonitorTest> getUrlUptimeMonitorTests(Long chplUptimeMonitorId) {
        return urlUptimeMonitorTestDAO.getChplUptimeMonitorTests(chplUptimeMonitorId);
    }

    private ServiceBaseUrlListUptimeReport summarize(UrlUptimeMonitor urlUptimeMonitor, List<UrlUptimeMonitorTest> urlUptimeMonitorTests) {
        LOGGER.info("Summarizing data for {}", urlUptimeMonitor.getUrl());

        List<UrlUptimeMonitorTest> allTestsForPastWeek = getEligibleTestsForPastWeek(urlUptimeMonitorTests);
        List<UrlUptimeMonitorTest> allTestsForCurrentMonth = getEligibleTestsForCurrentMonth(urlUptimeMonitorTests);

        return ServiceBaseUrlListUptimeReport.builder()
                .developerName(urlUptimeMonitor.getDeveloper().getName())
                .developerId(urlUptimeMonitor.getDeveloper().getId())
                .url(urlUptimeMonitor.getUrl())
                .totalTestCount(Long.valueOf(urlUptimeMonitorTests.size()))
                .totalSuccessfulTestCount(urlUptimeMonitorTests.stream()
                        .filter(test -> test.getPassed())
                        .count())
                .currentMonthTestCount(Long.valueOf(allTestsForCurrentMonth.size()))
                .currentMonthSuccessfulTestCount(allTestsForCurrentMonth.stream()
                        .filter(test -> test.getPassed())
                        .count())
                .pastWeekTestCount(Long.valueOf(allTestsForPastWeek.size()))
                .pastWeekSuccessfulTestCount(allTestsForPastWeek.stream()
                        .filter(test -> test.getPassed())
                        .count())
                .applicableAcbsMap(getApplicableAcbsMap(urlUptimeMonitor))
                .build();
    }

    private Map<Long, Boolean> getApplicableAcbsMap(UrlUptimeMonitor urlUptimeMonitor) {
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

    private List<UrlUptimeMonitorTest> getEligibleTestsForPastWeek(List<UrlUptimeMonitorTest> urlUptimeMonitorTests) {
        LocalDateTime end = LocalDateTime.now().minusDays(1).with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
        LocalDateTime start = LocalDateTime.now().minusWeeks(1).with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());

        return urlUptimeMonitorTests.stream()
                .filter(test -> test.getCheckTime().isAfter(start) && test.getCheckTime().isBefore(end))
                .toList();
    }

    private List<UrlUptimeMonitorTest> getEligibleTestsForCurrentMonth(List<UrlUptimeMonitorTest> urlUptimeMonitorTests) {
        LocalDateTime end = LocalDateTime.now().minusDays(1).with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
        LocalDateTime start = end.withDayOfMonth(1).with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());

        return urlUptimeMonitorTests.stream()
                .filter(test -> test.getCheckTime().isAfter(start) && test.getCheckTime().isBefore(end))
                .toList();
    }

}
