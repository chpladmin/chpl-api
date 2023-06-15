package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.datadog.api.client.v1.model.SyntheticsTestDetails;

import gov.healthit.chpl.domain.Developer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DatadogChplSynchonizer {
    // VARIABLE NAMING CONVENTION
    // ServiceBasedUrl - these are service based urls collected from the listings in CHPL
    // SyntheticTest - these are synthetic tests that exist in Datadog
    // DatadogMonitor - these are reporting entities that are store in the table datadog_monitors

    private DatadogSyntheticTestService datadogSyntheticTestService;
    private ServiceBasedUrlService serviceBasedUrlGatherer;
    private DatadogMonitorDAO datadogMonitorDAO;

    public DatadogChplSynchonizer(DatadogSyntheticTestService datadogSyntheticTestService, ServiceBasedUrlService serviceBasedUrlGatherer, DatadogMonitorDAO datadogMonitorDAO) {
        this.datadogSyntheticTestService = datadogSyntheticTestService;
        this.serviceBasedUrlGatherer = serviceBasedUrlGatherer;
        this.datadogMonitorDAO = datadogMonitorDAO;
    }

    public void synchronize() {
        synchronizeServiceBasedUrlAndSyntheticTests();
        synchronizeDatadogMonitorsAndSyntheticTests();
    }

    private void synchronizeServiceBasedUrlAndSyntheticTests() {
        List<ServiceBasedUrl> serviceBasedUrls = serviceBasedUrlGatherer.getAllServiceBasedUrls();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticTestService.getAllSyntheticTests();

        addMissingSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
        removeUnusedSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
    }

    private void synchronizeDatadogMonitorsAndSyntheticTests() {
        List<DatadogMonitor> datadogMonitors = datadogMonitorDAO.getAll();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticTestService.getAllSyntheticTests();

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
                .forEach(sbu -> datadogSyntheticTestService.createSyntheticApiTest(sbu.getUrl()));
    }

    private void removeUnusedSyntheticApiTests(List<ServiceBasedUrl> serviceBasedUrls, List<SyntheticsTestDetails> syntheticTestDetails) {
        List<SyntheticsTestDetails> syntheticsTestDetailsNotUsed = new ArrayList<SyntheticsTestDetails>(syntheticTestDetails);

        syntheticsTestDetailsNotUsed.removeIf(std -> serviceBasedUrls.stream()
                .filter(serviceBasedUrl -> serviceBasedUrl.getUrl().equals(std.getConfig().getRequest().getUrl()))
                .findAny()
                .isPresent());

        syntheticsTestDetailsNotUsed.stream()
                .peek(synthTestDet -> LOGGER.info("Removing the following URL from Datadog: {}", synthTestDet.getConfig().getRequest().getUrl()))
                .forEach(synthTestDet -> datadogSyntheticTestService.deleteSyntheticApiTest(List.of(synthTestDet.getPublicId())));
    }

    private void addMissingDatadogMonitorsToDb(List<SyntheticsTestDetails> syntheticTestDetails, List<DatadogMonitor> datadogMonitors) {
        List<SyntheticsTestDetails> syntheticTestsNotInDatdogMonitors = new ArrayList<SyntheticsTestDetails>(syntheticTestDetails);

        syntheticTestsNotInDatdogMonitors.removeIf(synthTest -> datadogMonitors.stream()
                .filter(monitor -> monitor.getUrl().equals(synthTest.getConfig().getRequest().getUrl()))
                .findAny()
                .isPresent());

        syntheticTestsNotInDatdogMonitors.stream()
                .peek(synthTest -> LOGGER.debug("Adding the following URL to datadog_monitor table: {}", synthTest.getConfig().getRequest().getUrl()))
                .forEach(synthTest -> addDatadogMonitor(DatadogMonitor.builder()
                        .datadogPublicId(synthTest.getPublicId())
                        .url(synthTest.getConfig().getRequest().getUrl())
                        .developer(Developer.builder()
                                .id(1628L)
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
}
