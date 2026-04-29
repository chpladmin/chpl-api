package gov.healthit.chpl.report.servicebaseurllistreport;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitor;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorDAO;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorTest;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorTestDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ServiceBaseUrlListReportService {
    private static final Double CONVERT_TO_PERCENT = 100.0;

    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    public ServiceBaseUrlListReportService(UrlUptimeMonitorDAO urlUptimeMonitorDAO,
            UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO,
            CertificationBodyManager certificationBodyManager) {
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
        this.certificationBodyManager = certificationBodyManager;
    }

    public List<UrlUptimeMonitorEx> getUrlUptimeMonitors() {
        return (List<UrlUptimeMonitorEx>) urlUptimeMonitorDAO.getAll().stream()
                .map(monitor -> UrlUptimeMonitorEx.builder()
                        .id(monitor.getId())
                        .developer(monitor.getDeveloper())
                        .url(monitor.getUrl())
                        .datadogPublicId(monitor.getDatadogPublicId())
                        .acbs(getAssocatedAcbs(monitor))
                        .tests(urlUptimeMonitorTestDAO.getChplUptimeMonitorTests(monitor.getId()).stream()
                                .filter(test -> test.getCheckTime().isAfter(LocalDateTime.now().minusYears(1)))
                                .toList())
                        .build())
                .toList();
    }

    public List<UrlUptimeMonitorSummary> getUrlUptimeMonitorsSummaries(Integer numDaysAgoMin, Integer numDaysAgoMax) {
        LocalDateTime minTestCheckTime = LocalDateTime.now().minusDays(numDaysAgoMax).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime maxTestCheckTime = LocalDateTime.now().minusDays(numDaysAgoMin).truncatedTo(ChronoUnit.DAYS);

        LOGGER.info("Finding URL Uptime Monitor Tests that happened between " + minTestCheckTime + " and " + maxTestCheckTime);
        return (List<UrlUptimeMonitorSummary>) urlUptimeMonitorDAO.getAll().stream()
                .map(monitor -> UrlUptimeMonitorSummary.builder()
                        .developer(monitor.getDeveloper())
                        .url(monitor.getUrl())
                        .percentPassed(calculatePercentPassedBetween(monitor.getId(), minTestCheckTime, maxTestCheckTime))
                        .build())
                .toList();
    }

    private List<IdNamePair> getAssocatedAcbs(UrlUptimeMonitor monitor) {
        if (monitor.getDelimitedAcbIds() == null || monitor.getDelimitedAcbIds().equals("")) {
            return List.of();
        }

        return Arrays.asList(monitor.getDelimitedAcbIds().split(",")).stream()
                .map(acbId -> Long.parseLong(acbId))
                .map(acbId -> {
                    try {
                        return certificationBodyManager.getById(acbId);
                    } catch (EntityRetrievalException e) {
                        return null;
                    }
                })
                .filter(acb -> acb != null && !acb.isRetired())
                .map(acb -> new IdNamePair(acb.getId(), acb.getName()))
                .toList();
    }

    private Double calculatePercentPassedBetween(Long monitorId, LocalDateTime minTestCheckTime, LocalDateTime maxTestCheckTime) {
        List<UrlUptimeMonitorTest> uptimeTestsWithinTimeWindow = urlUptimeMonitorTestDAO.getChplUptimeMonitorTests(monitorId).stream()
                .filter(test ->
                    (test.getCheckTime().isEqual(minTestCheckTime) || test.getCheckTime().isAfter(minTestCheckTime))
                    && (test.getCheckTime().isEqual(maxTestCheckTime) || test.getCheckTime().isBefore(maxTestCheckTime)))
                .toList();
        long totalTests = uptimeTestsWithinTimeWindow.size();
        long passedTests = uptimeTestsWithinTimeWindow.stream()
            .filter(test -> test.getPassed())
            .count();

        if (passedTests == 0 || totalTests == 0) {
            return 0.0;
        }
        return ((double) passedTests / totalTests) * CONVERT_TO_PERCENT;
    }
}
