package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.DayOfWeek;
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

@Log4j2(topic = "serviceBaseUrlListUptimeCreatorJobLogger")
@Component
public class DatadogUrlUptimeSynchonizer {
    private static final Long DAYS_TO_LOOK_BACK_FOR_RESULTS = 7L;

    // VARIABLE NAMING CONVENTION
    // ServiceBaseUrlList - these are service base url lists collected from the listings in CHPL, grouped by developer
    // DatadogSyntheticsTest - these are synthetic tests that exist in Datadog
    // UrlUptimeMonitor - these are reporting entities that are store in the table url_uptime_monitors

    private DatadogSyntheticsTestService datadogSyntheticsTestService;
    private DatadogSyntheticsTestResultService datadogSyntheticsTestResultService;
    private ServiceBaseUrlListService serviceBaseUrlListService;
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO utlUptimeMonitorTestDAO;


    public DatadogUrlUptimeSynchonizer(DatadogSyntheticsTestService datadogSyntheticsTestService, DatadogSyntheticsTestResultService datadogSyntheticsTestResultService,
            ServiceBaseUrlListService serviceBaseUrlListGatherer, UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO) {
        this.datadogSyntheticsTestService = datadogSyntheticsTestService;
        this.datadogSyntheticsTestResultService = datadogSyntheticsTestResultService;
        this.serviceBaseUrlListService = serviceBaseUrlListGatherer;
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.utlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
    }

    @Transactional
    public void synchronize() {
        //These must called in the order
        synchronizeDatadogSyntheticsTestsWithServiceBaseUrlLists();
        synchronizeUrlUptimeMonitorsWithDatadogSyntheticsTests();
        synchronizeUrlUptimeMonitorTestsWithDatadogSyntheticsTestResults();
    }

    private void synchronizeUrlUptimeMonitorTestsWithDatadogSyntheticsTestResults() {
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        getDatesToRetrieveResultsFor().stream()
                .peek(testDate -> LOGGER.info("**************** Retrieving test results for: {} ****************", testDate))
                .forEach(testDate -> urlUptimeMonitorDAO.getAll()
                        .forEach(urlUptimeMonitor ->  datadogSyntheticsTestResultService.getSyntheticsTestResults(getDatadogPublicId(
                                    syntheticsTestDetails,
                                    urlUptimeMonitor.getUrl(),
                                    urlUptimeMonitor.getDeveloper().getId()), testDate)
                                .forEach(syntheticsTestResult -> utlUptimeMonitorTestDAO.create(UrlUptimeMonitorTest.builder()
                                      .urlUptimeMonitorId(urlUptimeMonitor.getId())
                                      .checkTime(toLocalDateTime(syntheticsTestResult.getCheckTime().longValue()))
                                      .passed(syntheticsTestResult.getResult().getPassed())
                                      .build()))));
    }

    private void synchronizeDatadogSyntheticsTestsWithServiceBaseUrlLists() {
        List<ServiceBaseUrlList> serviceBaseUrlLists = serviceBaseUrlListService.getAllServiceBaseUrlLists();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        addMissingDatadogSyntheticApiTests(serviceBaseUrlLists, syntheticsTestDetails);
        removeUnusedDatadogSyntheticApiTests(serviceBaseUrlLists, syntheticsTestDetails);
    }

    private void synchronizeUrlUptimeMonitorsWithDatadogSyntheticsTests() {
        List<UrlUptimeMonitor> urlUptimeMonitors = urlUptimeMonitorDAO.getAll();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        addMissingUrlUptimeMonitorsToDb(syntheticsTestDetails, urlUptimeMonitors);
        removeUnusedUrlUptimeMonitors(syntheticsTestDetails, urlUptimeMonitors);
    }

    private void addMissingDatadogSyntheticApiTests(List<ServiceBaseUrlList> serviceBaseUrlLists, List<SyntheticsTestDetails> syntheticsTestDetails) {
        List<ServiceBaseUrlList> urlsNotInDatadog = new ArrayList<ServiceBaseUrlList>(serviceBaseUrlLists);
        urlsNotInDatadog.removeIf(sbu -> syntheticsTestDetails.stream()
                .filter(synthTest -> synthTest.getConfig().getRequest().getUrl().equals(sbu.getDatadogFormattedUrl())
                        && getDeveloperIdFromTags(synthTest.getTags()).equals(sbu.getDeveloperId()))
                .findAny()
                .isPresent());

        urlsNotInDatadog.stream()
                .peek(sbu -> LOGGER.info("Adding the following URL to Datadog: {}", sbu.getDatadogFormattedUrl()))
                .forEach(sbu -> datadogSyntheticsTestService.createSyntheticsTest(sbu.getDatadogFormattedUrl(), sbu.getDeveloperId()));
    }

    private void removeUnusedDatadogSyntheticApiTests(List<ServiceBaseUrlList> serviceBaseUrlListss, List<SyntheticsTestDetails> syntheticsTestDetails) {
        List<SyntheticsTestDetails> syntheticsTestDetailsNotUsed = new ArrayList<SyntheticsTestDetails>(syntheticsTestDetails);

        syntheticsTestDetailsNotUsed.removeIf(std -> serviceBaseUrlListss.stream()
                .filter(serviceBaseUrlList -> serviceBaseUrlList.getDatadogFormattedUrl().equals(std.getConfig().getRequest().getUrl())
                        && serviceBaseUrlList.getDeveloperId().equals(getDeveloperIdFromTags(std.getTags())))
                .findAny()
                .isPresent());

        syntheticsTestDetailsNotUsed.stream()
                .peek(synthTestDet -> LOGGER.info("Removing the following URL from Datadog: {}", synthTestDet.getConfig().getRequest().getUrl()))
                .forEach(synthTestDet -> datadogSyntheticsTestService.deleteSyntheticsTests(List.of(synthTestDet.getPublicId())));
    }

    private void addMissingUrlUptimeMonitorsToDb(List<SyntheticsTestDetails> syntheticsTestDetails, List<UrlUptimeMonitor> urlUptimeMonitors) {
        List<SyntheticsTestDetails> syntheticsTestsNotInDatdogMonitors = new ArrayList<SyntheticsTestDetails>(syntheticsTestDetails);

        syntheticsTestsNotInDatdogMonitors.removeIf(synthTest -> urlUptimeMonitors.stream()
                .filter(urlUptimeMonitor -> urlUptimeMonitor.getUrl().equals(synthTest.getConfig().getRequest().getUrl())
                        && urlUptimeMonitor.getDeveloper().getId().equals(getDeveloperIdFromTags(synthTest.getTags())))
                .findAny()
                .isPresent());

        syntheticsTestsNotInDatdogMonitors.stream()
                .peek(synthTest -> LOGGER.info("Adding the following URL to url_uptime_monitor table: {}", synthTest.getConfig().getRequest().getUrl()))
                .forEach(synthTest -> addUrlUptimeMonitor(UrlUptimeMonitor.builder()
                        .datadogPublicId(synthTest.getPublicId())
                        .url(synthTest.getConfig().getRequest().getUrl())
                        .developer(Developer.builder()
                                .id(getDeveloperIdFromTags(synthTest.getTags()))
                                .build())
                        .build()));
    }

    private void removeUnusedUrlUptimeMonitors(List<SyntheticsTestDetails> syntheticsTestDetails, List<UrlUptimeMonitor> urlUptimeMonitors) {
        List<UrlUptimeMonitor> urlUptimeMonitorsNotInSynthTests = new ArrayList<UrlUptimeMonitor>(urlUptimeMonitors);

        urlUptimeMonitorsNotInSynthTests.removeIf(monitor -> syntheticsTestDetails.stream()
                .filter(synthTest -> synthTest.getConfig().getRequest().getUrl().equals(monitor.getUrl())
                        && getDeveloperIdFromTags(synthTest.getTags()).equals(monitor.getDeveloper().getId()))
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
            if (!LocalDate.now().minusDays(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)
                    && !LocalDate.now().minusDays(i).getDayOfWeek().equals(DayOfWeek.SUNDAY)
                    && !doUrlUptimeMonitorTestsExistInDbForDate(LocalDate.now().minusDays(i))) {
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

    private String getDatadogPublicId(List<SyntheticsTestDetails> syntheticsTestDetails, String url, Long developerId) {
        return syntheticsTestDetails.stream()
                .filter(dets -> dets.getConfig().getRequest().getUrl().equals(url)
                        && getDeveloperIdFromTags(dets.getTags()).equals(developerId))
                .map(dets -> dets.getPublicId())
                .findAny()
                .get();

    }
    private LocalDateTime toLocalDateTime(Long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts),
                TimeZone.getDefault().toZoneId());
    }
}
