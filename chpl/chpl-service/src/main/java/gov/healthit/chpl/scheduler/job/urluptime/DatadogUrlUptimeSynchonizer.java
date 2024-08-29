package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
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
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;


    public DatadogUrlUptimeSynchonizer(DatadogSyntheticsTestService datadogSyntheticsTestService, DatadogSyntheticsTestResultService datadogSyntheticsTestResultService,
            ServiceBaseUrlListService serviceBaseUrlListGatherer, UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO) {
        this.datadogSyntheticsTestService = datadogSyntheticsTestService;
        this.datadogSyntheticsTestResultService = datadogSyntheticsTestResultService;
        this.serviceBaseUrlListService = serviceBaseUrlListGatherer;
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
    }

    @Transactional
    public void synchronize() {
        //These must called in the order
        synchronizeDatadogSyntheticsTestsWithServiceBaseUrlLists();
        synchronizeUrlUptimeMonitorsWithDatadogSyntheticsTests();
        synchronizeUrlUptimeMonitorTestsWithDatadogSyntheticsTestResults();
    }

    private void synchronizeUrlUptimeMonitorTestsWithDatadogSyntheticsTestResults() {
        LOGGER.info("**************** Getting Test Results from Datadog and saving it locally ****************");
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        getDatesToRetrieveResultsFor().stream()
                .peek(testDate -> LOGGER.info("**************** Retrieving test results for: {} ****************", testDate))
                .forEach(testDate -> urlUptimeMonitorDAO.getAll()
                        .forEach(urlUptimeMonitor ->  datadogSyntheticsTestResultService.getSyntheticsTestResults(
                                getDatadogPublicId(syntheticsTestDetails, urlUptimeMonitor.getUrl(), urlUptimeMonitor.getDeveloper().getId()),
                                testDate)
                                .forEach(syntheticsTestResult -> urlUptimeMonitorTestDAO.create(UrlUptimeMonitorTest.builder()
                                      .urlUptimeMonitorId(urlUptimeMonitor.getId())
                                      .datadogTestKey(syntheticsTestResult.getResultId())
                                      .checkTime(toLocalDateTime(syntheticsTestResult.getCheckTime().longValue()))
                                      .passed(syntheticsTestResult.getResult().getPassed())
                                      .build()))));
    }

    private void synchronizeDatadogSyntheticsTestsWithServiceBaseUrlLists() {
        LOGGER.info("**************** Synchronizing Datadog tests based on CHPL data ****************");
        createOrUpdateSyntheticsTest();
        removeUnusedDatadogSyntheticsTests();
    }

    private void createOrUpdateSyntheticsTest() {
        List<ServiceBaseUrlList> serviceBaseUrlLists = serviceBaseUrlListService.getAllServiceBaseUrlLists();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        for (ServiceBaseUrlList sbu : serviceBaseUrlLists) {
            Optional<SyntheticsTestDetails> foundSyntheticsTestDetails = findSyntheticsTestDetails(syntheticsTestDetails, sbu.getUrl());
            if (foundSyntheticsTestDetails.isEmpty()) {
                datadogSyntheticsTestService.createSyntheticsTest(sbu.getDatadogFormattedUrl(), sbu.getDeveloperIds());
                syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();
            } else {
                //Are the developer tags up to date?
                if (!CollectionUtils.isEqualCollection(getDeveloperIdsFromTags(foundSyntheticsTestDetails.get().getTags()), sbu.getDeveloperIds())) {
                    datadogSyntheticsTestService.setApplicableDevelopersForTest(foundSyntheticsTestDetails.get().getPublicId(), sbu.getDeveloperIds());
                    syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();
                }
            }
        }

    }

    private void removeUnusedDatadogSyntheticsTests() {
        List<ServiceBaseUrlList> serviceBaseUrlLists = serviceBaseUrlListService.getAllServiceBaseUrlLists();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        List<String> syntheticsTestPublicIdsToDelete = new ArrayList<String>();
        for (SyntheticsTestDetails std : syntheticsTestDetails) {
            boolean found = serviceBaseUrlLists.stream()
                    .filter(sbu -> sbu.getUrl().equals(std.getConfig().getRequest().getUrl()))
                    .findAny()
                    .isPresent();
            if (!found) {
                syntheticsTestPublicIdsToDelete.add(std.getPublicId());
            }
        }
        if (CollectionUtils.isNotEmpty(syntheticsTestPublicIdsToDelete)) {
            LOGGER.info("Removing Synthetics Test with PublicIds: {}", syntheticsTestPublicIdsToDelete);
            datadogSyntheticsTestService.deleteSyntheticsTests(syntheticsTestPublicIdsToDelete);
        }
    }

    private Optional<SyntheticsTestDetails> findSyntheticsTestDetails(List<SyntheticsTestDetails> syntheticsTestDetails, String url) {
        return syntheticsTestDetails.stream()
                .filter(std -> std.getConfig().getRequest().getUrl().equals(url))
                .findAny();
    }

    private void synchronizeUrlUptimeMonitorsWithDatadogSyntheticsTests() {
        LOGGER.info("**************** Synchronizing CHPL data based on Datadog tests ****************");
        List<UrlUptimeMonitor> urlUptimeMonitors = urlUptimeMonitorDAO.getAll();
        List<UrlUptimeMonitor> expectedUrlUptimeMonitors = generateExpectedUrlIUpTimeMonitors(datadogSyntheticsTestService.getAllSyntheticsTests());

        addMissingUrlMonitors(urlUptimeMonitors, expectedUrlUptimeMonitors);
        urlUptimeMonitors = urlUptimeMonitorDAO.getAll();
        removeOutdatedUrlMonitors(urlUptimeMonitors, expectedUrlUptimeMonitors);
    }

    private void addMissingUrlMonitors(List<UrlUptimeMonitor> existing, List<UrlUptimeMonitor> expected) {
        expected.stream()
                .filter(uum -> !contains(existing, uum))
                // Need to remove duplicates here
                .forEach(urlMonitor -> addUrlUptimeMonitor(urlMonitor));
    }

    private void removeOutdatedUrlMonitors(List<UrlUptimeMonitor> existing, List<UrlUptimeMonitor> expected) {
        existing.stream()
                .filter(uum -> !contains(expected, uum))
                .forEach(urlMonitor -> {
                    LOGGER.info("Removing the following URL to url_uptime_monitor table: {}, {}", urlMonitor.getUrl(), urlMonitor.getDeveloper().getId());
                    urlUptimeMonitorDAO.delete(urlMonitor);
                });
    }

    private Boolean contains(List<UrlUptimeMonitor> list, UrlUptimeMonitor value) {
        return list.stream()
                .anyMatch(item -> item.getDeveloper().getId().equals(value.getDeveloper().getId())
                        && item.getUrl().equals(value.getUrl()));
    }

    private List<UrlUptimeMonitor> generateExpectedUrlIUpTimeMonitors(List<SyntheticsTestDetails> syntheticsTestDetails) {
        return syntheticsTestDetails.stream()
                .flatMap(synthTest -> getDeveloperIdsFromTags(synthTest.getTags()).stream()
                        .map(devId -> UrlUptimeMonitor.builder()
                                .datadogPublicId(synthTest.getPublicId())
                                .url(synthTest.getConfig().getRequest().getUrl())
                                .developer(Developer.builder()
                                        .id(devId)
                                        .build())
                                .build()))
                .filter(distinctByKey(o -> o.getUrl() + " | " + o.getDeveloper().getId()))
                .toList();

    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<Object, Boolean>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private void addUrlUptimeMonitor(UrlUptimeMonitor urlUptimeMonitor) {
        try {
            LOGGER.info("Adding the following URL to url_uptime_monitor table: {}, {}", urlUptimeMonitor.getUrl(), urlUptimeMonitor.getDeveloper().getId());
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
        return urlUptimeMonitorTestDAO.getTestCountForDate(dateToCheck) > 0;
    }

    private List<Long> getDeveloperIdsFromTags(List<String> tags) {
        return tags.stream()
                .filter(tag -> tag.startsWith("developer:"))
                .map(tag -> Long.valueOf(tag.split(":")[1]))
                .toList();
    }

    private String getDatadogPublicId(List<SyntheticsTestDetails> syntheticsTestDetails, String url, Long developerId) {
        return syntheticsTestDetails.stream()
                .filter(dets -> dets.getConfig().getRequest().getUrl().equals(url)
                        && getDeveloperIdsFromTags(dets.getTags()).contains(developerId))
                .map(dets -> dets.getPublicId())
                .findAny()
                .get();

    }
    private LocalDateTime toLocalDateTime(Long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts),
                TimeZone.getDefault().toZoneId());
    }


}
