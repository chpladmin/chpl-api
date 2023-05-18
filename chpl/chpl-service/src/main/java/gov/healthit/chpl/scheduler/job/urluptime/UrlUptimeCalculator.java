package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "urlUptimeEmailJobLogger")
@Component
public class UrlUptimeCalculator {

    private ChplUptimeMonitorDAO chplUptimeMonitorDAO;
    private ChplUptimeMonitorTestDAO chplUptimeMonitorTestDAO;

    @Autowired
    public UrlUptimeCalculator(ChplUptimeMonitorDAO chplUptimeMonitorDAO, ChplUptimeMonitorTestDAO chplUptimeMonitorTestDAO) {
        this.chplUptimeMonitorDAO = chplUptimeMonitorDAO;
        this.chplUptimeMonitorTestDAO = chplUptimeMonitorTestDAO;
    }

    public List<UrlUptimeReport> calculateRowsForReport() {
        List<UrlUptimeReport> reports = new ArrayList<UrlUptimeReport>();

        getChplUptimeMonitors().stream()
                .forEach(monitor -> reports.add(summarize(monitor, getChplUptimeMonitorTests(monitor.getId()))));
        return reports;
    }

    private List<ChplUptimeMonitor> getChplUptimeMonitors() {
        return chplUptimeMonitorDAO.getAll();
    }

    private List<ChplUptimeMonitorTest> getChplUptimeMonitorTests(Long chplUptimeMonitorId) {
        return chplUptimeMonitorTestDAO.getChplUptimeMonitorTests(chplUptimeMonitorId);
    }

    private UrlUptimeReport summarize(ChplUptimeMonitor chplUptimeMonitor, List<ChplUptimeMonitorTest> chplUptimeMonitorTests) {
        LOGGER.info("Summarizing data for {}", chplUptimeMonitor.getUrl());
        return UrlUptimeReport.builder()
                .description(chplUptimeMonitor.getDescription())
                .url(chplUptimeMonitor.getUrl())
                .testCount(Long.valueOf(chplUptimeMonitorTests.size()))
                .successfulTestCount(chplUptimeMonitorTests.stream()
                        .filter(test -> test.getPassed())
                        .count())
                .build();
    }
}
