package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.datadog.api.client.v1.model.SyntheticsTestDetails;

import gov.healthit.chpl.domain.Developer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DatadogUrlUptimeSynchonizer {
    private static final Long DAYS_TO_LOOK_BACK_FOR_RESULTS = 7L;

    // VARIABLE NAMING CONVENTION
    // ServiceBasedUrl - these are service based urls collected from the listings in CHPL, grouped by developer
    // DatadogSyntheticTest - these are synthetic tests that exist in Datadog
    // UrlUptimeMonitor - these are reporting entities that are store in the table url_uptime_monitors

    private DatadogSyntheticTestService datadogSyntheticTestService;
    private DatadogSyntheticTestResultService datadogSyntheticTestResultService;
    private ServiceBasedUrlService serviceBasedUrlGatherer;
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO utlUptimeMonitorTestDAO;


    public DatadogUrlUptimeSynchonizer(DatadogSyntheticTestService datadogSyntheticTestService, DatadogSyntheticTestResultService datadogSyntheticTestResultService,
            ServiceBasedUrlService serviceBasedUrlGatherer, UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO) {
        this.datadogSyntheticTestService = datadogSyntheticTestService;
        this.datadogSyntheticTestResultService = datadogSyntheticTestResultService;
        this.serviceBasedUrlGatherer = serviceBasedUrlGatherer;
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.utlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
    }

    @Transactional
    public void synchronize() {
        //These must called in the order
        synchronizeDatadogSyntheticTestsWithServiceBasedUrl();
        synchronizeUrlUptimeMonitorsWithDatadogSyntheticTests();
        synchronizeUrlUptimeMonitorTestsWithDatadogSyntheticTestResults();
    }

    private void synchronizeUrlUptimeMonitorTestsWithDatadogSyntheticTestResults() {
        getDatesToRetrieveResultsFor().stream()
                .peek(testDate -> LOGGER.info("**************** Retrieving test results for: {} ****************", testDate))
                .forEach(testDate -> urlUptimeMonitorDAO.getAll()
                        .forEach(urlUptimeMonitor ->  datadogSyntheticTestResultService.getSyntheticTestResults(urlUptimeMonitor.getDatadogPublicId(), testDate)
                                .forEach(syntheticTestResult -> utlUptimeMonitorTestDAO.create(UrlUptimeMonitorTest.builder()
                                      .urlUptimeMonitorId(urlUptimeMonitor.getId())
                                      .datadogTestKey(syntheticTestResult.getResultId())
                                      .checkTime(toLocalDateTime(syntheticTestResult.getCheckTime().longValue()))
                                      .passed(syntheticTestResult.getResult().getPassed())
                                      .build()))));
    }

    private void synchronizeDatadogSyntheticTestsWithServiceBasedUrl() {
        List<ServiceBasedUrl> serviceBasedUrls = serviceBasedUrlGatherer.getAllServiceBasedUrls();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticTestService.getAllSyntheticTests();

        addMissingDatadogSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
        removeUnusedDatadogSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
    }

    private void synchronizeUrlUptimeMonitorsWithDatadogSyntheticTests() {
        List<UrlUptimeMonitor> urlUptimeMonitors = urlUptimeMonitorDAO.getAll();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticTestService.getAllSyntheticTests();

        addMissingUrlUptimeMonitorsToDb(syntheticsTestDetails, urlUptimeMonitors);
        removeUnusedUrlUptimeMonitors(syntheticsTestDetails, urlUptimeMonitors);;
    }

    private void addMissingDatadogSyntheticApiTests(List<ServiceBasedUrl> serviceBasedUrls, List<SyntheticsTestDetails> syntheticTestDetails) {
        List<ServiceBasedUrl> urlsNotInDatadog = new ArrayList<ServiceBasedUrl>(serviceBasedUrls);
        urlsNotInDatadog.removeIf(sbu -> syntheticTestDetails.stream()
                .filter(synthTest -> synthTest.getConfig().getRequest().getUrl().equals(sbu.getUrl()))
                .findAny()
                .isPresent());

        urlsNotInDatadog.stream()
                .peek(sbu -> LOGGER.info("Adding the following URL to Datadog: {}", sbu.getUrl()))
                .forEach(sbu -> datadogSyntheticTestService.createSyntheticApiTest(sbu.getUrl(), sbu.getDeveloperId()));
    }

    private void removeUnusedDatadogSyntheticApiTests(List<ServiceBasedUrl> serviceBasedUrls, List<SyntheticsTestDetails> syntheticTestDetails) {
        List<SyntheticsTestDetails> syntheticsTestDetailsNotUsed = new ArrayList<SyntheticsTestDetails>(syntheticTestDetails);

        syntheticsTestDetailsNotUsed.removeIf(std -> serviceBasedUrls.stream()
                .filter(serviceBasedUrl -> serviceBasedUrl.getUrl().equals(std.getConfig().getRequest().getUrl()))
                .findAny()
                .isPresent());

        syntheticsTestDetailsNotUsed.stream()
                .peek(synthTestDet -> LOGGER.info("Removing the following URL from Datadog: {}", synthTestDet.getConfig().getRequest().getUrl()))
                .forEach(synthTestDet -> datadogSyntheticTestService.deleteSyntheticApiTest(List.of(synthTestDet.getPublicId())));
    }

    private void addMissingUrlUptimeMonitorsToDb(List<SyntheticsTestDetails> syntheticTestDetails, List<UrlUptimeMonitor> urlUptimeMonitors) {
        List<SyntheticsTestDetails> syntheticTestsNotInDatdogMonitors = new ArrayList<SyntheticsTestDetails>(syntheticTestDetails);

        syntheticTestsNotInDatdogMonitors.removeIf(synthTest -> urlUptimeMonitors.stream()
                .filter(urlUptimeMonitor -> urlUptimeMonitor.getUrl().equals(synthTest.getConfig().getRequest().getUrl()))
                .findAny()
                .isPresent());

        syntheticTestsNotInDatdogMonitors.stream()
                .peek(synthTest -> LOGGER.info("Adding the following URL to url_uptime_monitor table: {}", synthTest.getConfig().getRequest().getUrl()))
                .forEach(synthTest -> addUrlUptimeMonitor(UrlUptimeMonitor.builder()
                        .datadogPublicId(synthTest.getPublicId())
                        .url(synthTest.getConfig().getRequest().getUrl())
                        .developer(Developer.builder()
                                .id(getDeveloperIdFromTags(synthTest.getTags()))
                                .build())
                        .build()));
    }

    private void removeUnusedUrlUptimeMonitors(List<SyntheticsTestDetails> syntheticTestDetails, List<UrlUptimeMonitor> urlUptimeMonitors) {
        List<UrlUptimeMonitor> urlUptimeMonitorsNotInSynthTests = new ArrayList<UrlUptimeMonitor>(urlUptimeMonitors);

        urlUptimeMonitorsNotInSynthTests.removeIf(monitor -> syntheticTestDetails.stream()
                .filter(synthTest -> synthTest.getConfig().getRequest().getUrl().equals(monitor.getUrl()))
                .findAny()
                .isPresent());

        urlUptimeMonitorsNotInSynthTests.stream()
                .peek(monitor -> LOGGER.info("Removing the following URL from url_uptime_monitor table: {}", monitor.getUrl()))
                .forEach(monitor -> urlUptimeMonitorDAO.delete(monitor));
    }

    private void addUrlUptimeMonitor(UrlUptimeMonitor urlUptimeMonitor) {
        try {
            urlUptimeMonitorDAO.create(urlUptimeMonitor);
        } catch (Exception e) {
            LOGGER.error("Could not add the following URL to url_uptime_monitor table: {}", urlUptimeMonitor.getUrl(), e);
        }
    }

    private List<LocalDate> getDatesToRetrieveResultsFor() {
        List<LocalDate> datesToRetrieveResultsFor = new ArrayList<LocalDate>();
        for (Long i = 1L; i <= DAYS_TO_LOOK_BACK_FOR_RESULTS; ++i) {
            if (!doUrlUptimeMonitorTestsExistInDbForDate(LocalDate.now().minusDays(i))) {
                LOGGER.info("Retieve datadog monitor results for {}: YES", LocalDate.now().minusDays(i));
                datesToRetrieveResultsFor.add(LocalDate.now().minusDays(i));
            } else {
                LOGGER.info("Retieve datadog monitor results for {}: NO", LocalDate.now().minusDays(i));
            }
        }
        return datesToRetrieveResultsFor;
    }

    private Boolean doUrlUptimeMonitorTestsExistInDbForDate(LocalDate dateToCheck) {
        return utlUptimeMonitorTestDAO.getTestCountForDate(dateToCheck) > 0;
    }

    private Long getDeveloperIdFromTags(List<String> tags) {
        String developerTag = tags.stream()
                .filter(tag -> tag.startsWith("developer:"))
                .findAny()
                .orElse(null);

        if (developerTag == null) {
            return null;
        } else {
            return Long.valueOf(developerTag.split(":")[1]);
        }
    }

    private LocalDateTime toLocalDateTime(Long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts),
                TimeZone.getDefault().toZoneId());
    }
}
