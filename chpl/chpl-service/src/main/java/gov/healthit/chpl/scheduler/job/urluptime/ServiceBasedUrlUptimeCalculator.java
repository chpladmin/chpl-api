package gov.healthit.chpl.scheduler.job.urluptime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "urlUptimeEmailJobLogger")
@Component
public class ServiceBasedUrlUptimeCalculator {
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;

    @Autowired
    public ServiceBasedUrlUptimeCalculator(UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO) {
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
    }

//    public List<UrlUptimeReport> calculateRowsForReport() {
//        List<UrlUptimeReport> reports = new ArrayList<UrlUptimeReport>();
//
//        getChplUptimeMonitors().stream()
//                .forEach(monitor -> reports.add(summarize(monitor, getChplUptimeMonitorTests(monitor.getId()))));
//        return reports;
//    }
//
//    private List<ChplUptimeMonitor> getChplUptimeMonitors() {
//        return chplUptimeMonitorDAO.getAll();
//    }
//
//    private List<ChplUptimeMonitorTest> getChplUptimeMonitorTests(Long chplUptimeMonitorId) {
//        return chplUptimeMonitorTestDAO.getChplUptimeMonitorTests(chplUptimeMonitorId);
//    }
//
//    private UrlUptimeReport summarize(ChplUptimeMonitor chplUptimeMonitor, List<ChplUptimeMonitorTest> chplUptimeMonitorTests) {
//        LOGGER.info("Summarizing data for {}", chplUptimeMonitor.getUrl());
//
//        List<ChplUptimeMonitorTest> allTestsForPastWeek = getEligibleTestsForPastWeek(chplUptimeMonitorTests);
//        List<ChplUptimeMonitorTest> allTestsForCurrentMonth = getEligibleTestsForCurrentMonth(chplUptimeMonitorTests);
//
//        return UrlUptimeReport.builder()
//                .description(chplUptimeMonitor.getDescription())
//                .url(chplUptimeMonitor.getUrl())
//                .totalTestCount(Long.valueOf(chplUptimeMonitorTests.size()))
//                .totalSuccessfulTestCount(chplUptimeMonitorTests.stream()
//                        .filter(test -> test.getPassed())
//                        .count())
//                .currentMonthTestCount(Long.valueOf(allTestsForCurrentMonth.size()))
//                .currentMonthSuccessfulTestCount(allTestsForCurrentMonth.stream()
//                        .filter(test -> test.getPassed())
//                        .count())
//                .pastWeekTestCount(Long.valueOf(allTestsForPastWeek.size()))
//                .pastWeekSuccessfulTestCount(allTestsForPastWeek.stream()
//                        .filter(test -> test.getPassed())
//                        .count())
//                .build();
//    }
//
//    private List<ChplUptimeMonitorTest> getEligibleTestsForPastWeek(List<ChplUptimeMonitorTest> chplUptimeMonitorTests) {
//        LocalDateTime end = LocalDateTime.now().minusDays(1).with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
//        LocalDateTime start = LocalDateTime.now().minusWeeks(1).with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());
//
//        return chplUptimeMonitorTests.stream()
//                .filter(test -> test.getCheckTime().isAfter(start) && test.getCheckTime().isBefore(end))
//                .toList();
//    }
//
//    private List<ChplUptimeMonitorTest> getEligibleTestsForCurrentMonth(List<ChplUptimeMonitorTest> chplUptimeMonitorTests) {
//        LocalDateTime end = LocalDateTime.now().minusDays(1).with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
//        LocalDateTime start = end.withDayOfMonth(1).with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());
//
//        return chplUptimeMonitorTests.stream()
//                .filter(test -> test.getCheckTime().isAfter(start) && test.getCheckTime().isBefore(end))
//                .toList();
//    }

}
