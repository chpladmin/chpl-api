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
public class DatadogChplSynchonizer {
    private static final Long DAYS_TO_LOOK_BACK_FOR_RESULTS = 5L;

    // VARIABLE NAMING CONVENTION
    // ServiceBasedUrl - these are service based urls collected from the listings in CHPL, grouped by developer
    // SyntheticTest - these are synthetic tests that exist in Datadog
    // DatadogMonitor - these are reporting entities that are store in the table datadog_monitors

    private SyntheticTestService syntheticTestService;
    private SyntheticTestResultService syntheticTestResultService;
    private ServiceBasedUrlService serviceBasedUrlGatherer;
    private DatadogMonitorDAO datadogMonitorDAO;
    private DatadogMonitorTestDAO datadogMonitorTestDAO;


    public DatadogChplSynchonizer(SyntheticTestService syntheticTestService, SyntheticTestResultService syntheticTestResultService, ServiceBasedUrlService serviceBasedUrlGatherer,
            DatadogMonitorDAO datadogMonitorDAO, DatadogMonitorTestDAO datadogMonitorTestDAO) {
        this.syntheticTestService = syntheticTestService;
        this.syntheticTestResultService = syntheticTestResultService;
        this.serviceBasedUrlGatherer = serviceBasedUrlGatherer;
        this.datadogMonitorDAO = datadogMonitorDAO;
        this.datadogMonitorTestDAO = datadogMonitorTestDAO;
    }

    @Transactional
    public void synchronize() {
        //These must called in the order
        synchronizeServiceBasedUrlAndSyntheticTests();
        synchronizeDatadogMonitorsAndSyntheticTests();
        synchronizeSyntheticTestResultsAndDatadogMonitorTests();
    }

    private void synchronizeSyntheticTestResultsAndDatadogMonitorTests() {
        getDatesToRetrieveResultsFor().stream()
                .peek(testDate -> LOGGER.info("**************** Retrieving test results for: {} ****************", testDate))
                .forEach(testDate -> datadogMonitorDAO.getAll()
                        .forEach(datadogMonitor ->  syntheticTestResultService.getSyntheticTestResults(datadogMonitor.getDatadogPublicId(), testDate)
                                .forEach(result -> datadogMonitorTestDAO.create(DatadogMonitorTest.builder()
                                      .datadogMonitorId(datadogMonitor.getId())
                                      .datadogTestKey(result.getResultId())
                                      .checkTime(toLocalDateTime(result.getCheckTime().longValue()))
                                      .passed(result.getResult().getPassed())
                                      .build()))));
    }

    private LocalDateTime toLocalDateTime(Long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts),
                TimeZone.getDefault().toZoneId());
    }

    private List<LocalDate> getDatesToRetrieveResultsFor() {
        List<LocalDate> datesToRetrieveResultsFor = new ArrayList<LocalDate>();

        for (Long i = 1L; i <= DAYS_TO_LOOK_BACK_FOR_RESULTS; ++i) {
            if (!doDatadogMonitorTestsExistInDbForDate(LocalDate.now().minusDays(i))) {
                datesToRetrieveResultsFor.add(LocalDate.now().minusDays(i));
            }
        }

        return datesToRetrieveResultsFor;
    }

    private Boolean doDatadogMonitorTestsExistInDbForDate(LocalDate dateToCheck) {
        return datadogMonitorTestDAO.getTestCountForDate(dateToCheck) > 0;
    }

    private void synchronizeServiceBasedUrlAndSyntheticTests() {
        List<ServiceBasedUrl> serviceBasedUrls = serviceBasedUrlGatherer.getAllServiceBasedUrls();
        List<SyntheticsTestDetails> syntheticsTestDetails = syntheticTestService.getAllSyntheticTests();

        addMissingSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
        removeUnusedSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
    }

    private void synchronizeDatadogMonitorsAndSyntheticTests() {
        List<DatadogMonitor> datadogMonitors = datadogMonitorDAO.getAll();
        List<SyntheticsTestDetails> syntheticsTestDetails = syntheticTestService.getAllSyntheticTests();

        addMissingDatadogMonitorsToDb(syntheticsTestDetails, datadogMonitors);
        removeUnusedDatadogMonitors(syntheticsTestDetails, datadogMonitors);;
    }

    private void addMissingSyntheticApiTests(List<ServiceBasedUrl> serviceBasedUrls, List<SyntheticsTestDetails> syntheticTestDetails) {
        List<ServiceBasedUrl> urlsNotInDatadog = new ArrayList<ServiceBasedUrl>(serviceBasedUrls);
        urlsNotInDatadog.removeIf(sbu -> syntheticTestDetails.stream()
                .filter(synthTest -> synthTest.getConfig().getRequest().getUrl().equals(sbu.getUrl()))
                .findAny()
                .isPresent());

        urlsNotInDatadog.stream()
                .peek(sbu -> LOGGER.info("Adding the following URL to Datadog: {}", sbu.getUrl()))
                .forEach(sbu -> syntheticTestService.createSyntheticApiTest(sbu.getUrl(), sbu.getDeveloperId()));
    }

    private void removeUnusedSyntheticApiTests(List<ServiceBasedUrl> serviceBasedUrls, List<SyntheticsTestDetails> syntheticTestDetails) {
        List<SyntheticsTestDetails> syntheticsTestDetailsNotUsed = new ArrayList<SyntheticsTestDetails>(syntheticTestDetails);

        syntheticsTestDetailsNotUsed.removeIf(std -> serviceBasedUrls.stream()
                .filter(serviceBasedUrl -> serviceBasedUrl.getUrl().equals(std.getConfig().getRequest().getUrl()))
                .findAny()
                .isPresent());

        syntheticsTestDetailsNotUsed.stream()
                .peek(synthTestDet -> LOGGER.info("Removing the following URL from Datadog: {}", synthTestDet.getConfig().getRequest().getUrl()))
                .forEach(synthTestDet -> syntheticTestService.deleteSyntheticApiTest(List.of(synthTestDet.getPublicId())));
    }

    private void addMissingDatadogMonitorsToDb(List<SyntheticsTestDetails> syntheticTestDetails, List<DatadogMonitor> datadogMonitors) {
        List<SyntheticsTestDetails> syntheticTestsNotInDatdogMonitors = new ArrayList<SyntheticsTestDetails>(syntheticTestDetails);

        syntheticTestsNotInDatdogMonitors.removeIf(synthTest -> datadogMonitors.stream()
                .filter(monitor -> monitor.getUrl().equals(synthTest.getConfig().getRequest().getUrl()))
                .findAny()
                .isPresent());

        syntheticTestsNotInDatdogMonitors.stream()
                .peek(synthTest -> LOGGER.info("Adding the following URL to datadog_monitor table: {}", synthTest.getConfig().getRequest().getUrl()))
                .forEach(synthTest -> addDatadogMonitor(DatadogMonitor.builder()
                        .datadogPublicId(synthTest.getPublicId())
                        .url(synthTest.getConfig().getRequest().getUrl())
                        .developer(Developer.builder()
                                .id(getDeveloperIdFromTags(synthTest.getTags()))
                                .build())
                        .build()));
    }

    private void removeUnusedDatadogMonitors(List<SyntheticsTestDetails> syntheticTestDetails, List<DatadogMonitor> datadogMonitors) {
        List<DatadogMonitor> datadogMonitorsNotInSynthTests = new ArrayList<DatadogMonitor>(datadogMonitors);

        datadogMonitorsNotInSynthTests.removeIf(monitor -> syntheticTestDetails.stream()
                .filter(synthTest -> synthTest.getConfig().getRequest().getUrl().equals(monitor.getUrl()))
                .findAny()
                .isPresent());

        datadogMonitorsNotInSynthTests.stream()
                .peek(monitor -> LOGGER.info("Removing the following URL from datadog_monitor table: {}", monitor.getUrl()))
                .forEach(monitor -> datadogMonitorDAO.delete(monitor));
    }

    private void addDatadogMonitor(DatadogMonitor datadogMonitor) {
        try {
            datadogMonitorDAO.create(datadogMonitor);
        } catch (Exception e) {
            LOGGER.error("Could not add the following URL to datadog_monitor table: {}", datadogMonitor.getUrl(), e);
        }
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
}
