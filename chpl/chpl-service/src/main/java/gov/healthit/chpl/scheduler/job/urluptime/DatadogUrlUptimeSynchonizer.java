package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.datadog.api.client.v1.model.SyntheticsAPITestResultFull;
import com.datadog.api.client.v1.model.SyntheticsAPITestResultShort;
import com.datadog.api.client.v1.model.SyntheticsApiTestFailureCode;
import com.datadog.api.client.v1.model.SyntheticsTestDetailsWithoutSteps;

import gov.healthit.chpl.datadog.OnDemandUrlCheckerManager;
import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResponse;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.ValidationException;
import io.github.bucket4j.Bucket;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBaseUrlListUptimeCreatorJobLogger")
@Component
public class DatadogUrlUptimeSynchonizer {
    private static final Long DAYS_TO_LOOK_BACK_FOR_RESULTS = 7L;
    private static final long DATADOG_REQUESTS_PER_MINUTE = 90; //the actual limit is 100 but I don't feel like we need to cut it that close
    private static final long DATADOG_REQUESTS_BURST_LIMIT = 5;

    // VARIABLE NAMING CONVENTION
    // ServiceBaseUrlList - these are service base url lists collected from the listings in CHPL, grouped by developer
    // DatadogSyntheticsTest - these are synthetic tests that exist in Datadog
    // UrlUptimeMonitor - these are reporting entities that are store in the table url_uptime_monitor

    private DatadogSyntheticsTestService datadogSyntheticsTestService;
    private DatadogSyntheticsTestResultService datadogSyntheticsTestResultService;
    private ServiceBaseUrlListService serviceBaseUrlListService;
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;
    private DeveloperSearchService developerSearchService;
    private Bucket bucket;

    private List<String> errorsToIgnore;


    public DatadogUrlUptimeSynchonizer(DatadogSyntheticsTestService datadogSyntheticsTestService, DatadogSyntheticsTestResultService datadogSyntheticsTestResultService,
            ServiceBaseUrlListService serviceBaseUrlListGatherer, UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO,
            DeveloperSearchService developerSearchService) {
        this.datadogSyntheticsTestService = datadogSyntheticsTestService;
        this.datadogSyntheticsTestResultService = datadogSyntheticsTestResultService;
        this.serviceBaseUrlListService = serviceBaseUrlListGatherer;
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
        this.developerSearchService = developerSearchService;
        this.bucket = Bucket.builder()
                // 1. Sustained Limit: 90 requests per 1 minute
                .addLimit(limit -> limit
                    .capacity(DATADOG_REQUESTS_PER_MINUTE)
                    .refillGreedy(DATADOG_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                // 2. Burst Limit: Maximum 5 requests per 1 second
                .addLimit(limit -> limit
                    .capacity(DATADOG_REQUESTS_BURST_LIMIT)
                    .refillGreedy(DATADOG_REQUESTS_BURST_LIMIT, Duration.ofSeconds(1)))
                .build();
        errorsToIgnore = List.of("BODY_TOO_LARGE_TO_PROCESS");
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
        List<SyntheticsTestDetailsWithoutSteps> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        getDatesToRetrieveResultsFor().stream()
                .peek(testDate -> LOGGER.info("**************** Retrieving test results for: {} ****************", testDate))
                .forEach(testDate -> urlUptimeMonitorDAO.getAll().forEach(urlUptimeMonitor -> {
                        String publicId = getDatadogPublicId(syntheticsTestDetails, urlUptimeMonitor.getUrl(), urlUptimeMonitor.getDeveloper().getId());
                        datadogSyntheticsTestResultService.getSyntheticsTestResults(publicId, testDate).forEach(syntheticsTestResult -> {
                            try {
                                //Blocks until tokens for BOTH limits are available
                                bucket.asBlocking().consumeUninterruptibly(1);
                                urlUptimeMonitorTestDAO.create(UrlUptimeMonitorTest.builder()
                                        .urlUptimeMonitorId(urlUptimeMonitor.getId())
                                        .datadogTestKey(syntheticsTestResult.getResultId())
                                        .checkTime(toLocalDateTime(syntheticsTestResult.getCheckTime().longValue()))
                                        .passed(calculatePassed(syntheticsTestResult, publicId))
                                        .build());
                            } catch (Exception ex) {
                                LOGGER.error("Could not process url_uptime_monitor " + publicId + " for url " + urlUptimeMonitor.getUrl() + " and developer " + urlUptimeMonitor.getDeveloper().getId());
                            }
                        });
                }));
    }

    private boolean calculatePassed(SyntheticsAPITestResultShort result, String publicId) {
        if (result.getResult().getPassed()) {
            return true;
        } else {
            SyntheticsAPITestResultFull detailedResult = datadogSyntheticsTestResultService.getDetailedTestResult(publicId, result.getResultId());
            return detailedResult != null && isErrorIgnorable(detailedResult.getResult().getFailure().getCode());
        }
    }

    private boolean isErrorIgnorable(SyntheticsApiTestFailureCode errorCode) {
        return errorsToIgnore.stream()
                .filter(code -> code.equals(errorCode.getValue()))
                .findAny()
                .isPresent();
    }

    private void synchronizeDatadogSyntheticsTestsWithServiceBaseUrlLists() {
        LOGGER.info("**************** Synchronizing Datadog tests based on CHPL data ****************");
        createOrUpdateSyntheticsTest();
        removeUnusedDatadogSyntheticsTests();
    }

    private void createOrUpdateSyntheticsTest() {
        List<ServiceBaseUrlList> serviceBaseUrlLists = serviceBaseUrlListService.getAllServiceBaseUrlLists();
        List<SyntheticsTestDetailsWithoutSteps> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        for (ServiceBaseUrlList sbu : serviceBaseUrlLists) {
            Optional<SyntheticsTestDetailsWithoutSteps> foundSyntheticsTestDetails = findSyntheticsTestDetails(syntheticsTestDetails, sbu.getUrl());
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
        List<SyntheticsTestDetailsWithoutSteps> syntheticsTestDetails = datadogSyntheticsTestService.getAllSyntheticsTests();

        List<String> syntheticsTestPublicIdsToDelete = new ArrayList<String>();
        for (SyntheticsTestDetailsWithoutSteps std : syntheticsTestDetails) {
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

    private Optional<SyntheticsTestDetailsWithoutSteps> findSyntheticsTestDetails(List<SyntheticsTestDetailsWithoutSteps> syntheticsTestDetails, String url) {
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

        updateAcbsForUrlUptimeMonitors(urlUptimeMonitors);

    }

    private void updateAcbsForUrlUptimeMonitors(List<UrlUptimeMonitor> urlUptimeMonitors) {
        try {
            urlUptimeMonitors.forEach(monitor -> updateAcbsForUrlUptimeMonitor(monitor));
        } catch (Exception e) {
            LOGGER.error("Could not update ACBs for URL Uptime Monitors", e);
        }
    }

    private void updateAcbsForUrlUptimeMonitor(UrlUptimeMonitor urlUptimeMonitor) {
        try {
            urlUptimeMonitorDAO.updateAcbsForMonitor(urlUptimeMonitor, getCertificationBodies(urlUptimeMonitor.getDeveloper().getId()));
        } catch (Exception e) {
            LOGGER.error("Could not update ACBs for URL Uptime Monitor with Id: {}", urlUptimeMonitor.getId(), e);
        }
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
                        LOGGER.info("Removing the following URL to url_uptime_monitor table: {}, {}, {}", urlMonitor.getUrl(), urlMonitor.getDeveloper().getId(), urlMonitor.getDatadogPublicId());
                        urlUptimeMonitorDAO.delete(urlMonitor);
                });
    }

    private Boolean contains(List<UrlUptimeMonitor> list, UrlUptimeMonitor value) {
        return list.stream()
                .anyMatch(item -> item.getDeveloper().getId().equals(value.getDeveloper().getId())
                        && item.getUrl().equals(value.getUrl()));
    }

    private List<UrlUptimeMonitor> generateExpectedUrlIUpTimeMonitors(List<SyntheticsTestDetailsWithoutSteps> syntheticsTestDetails) {
        return (List<UrlUptimeMonitor>) syntheticsTestDetails.stream()
                .flatMap(synthTest -> getDeveloperIdsFromTags(synthTest.getTags()).stream()
                        .map(devId -> UrlUptimeMonitor.builder()
                                .datadogPublicId(synthTest.getPublicId())
                                .url(synthTest.getConfig().getRequest().getUrl())
                                .developer(Developer.builder()
                                        .id(devId)
                                        .build())
                                .delimitedAcbIds(null)
                                .build()))
                .filter(distinctByKey(o -> o.getUrl() + " | " + o.getDeveloper().getId()))
                .toList();

    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<Object, Boolean>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private void addUrlUptimeMonitor(UrlUptimeMonitor urlUptimeMonitor) {
        // Ignore monitors URL monitors with a developer of -99.  These are from the On Demand Checker.
        if (urlUptimeMonitor.getDeveloper().getId() != OnDemandUrlCheckerManager.TEMP_DEVELOPER_ID) {
            try {
                LOGGER.info("Adding the following URL to url_uptime_monitor table: {}, {}", urlUptimeMonitor.getUrl(), urlUptimeMonitor.getDeveloper().getId());
                urlUptimeMonitorDAO.create(urlUptimeMonitor);
            } catch (Exception e) {
                LOGGER.error("Could not add the following URL to url_uptime_monitor table: {}", urlUptimeMonitor.getUrl(), e);
            }
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

    private String getDatadogPublicId(List<SyntheticsTestDetailsWithoutSteps> syntheticsTestDetails, String url, Long developerId) {
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

    private List<CertificationBody> getCertificationBodies(Long developerId) {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
                .developerIds(Set.of(developerId))
                .build();

        try {
            DeveloperSearchResponse response = developerSearchService.findDevelopers(request);
            return response.getResults().get(0).getAcbsForActiveListings().stream()
                    .map(acb -> CertificationBody.builder()
                            .id(acb.getId())
                            .name(acb.getName())
                            .build())
                    .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not get ACBs for developer with ID: {}", developerId, e);
            return List.of();
        }
    }

}
