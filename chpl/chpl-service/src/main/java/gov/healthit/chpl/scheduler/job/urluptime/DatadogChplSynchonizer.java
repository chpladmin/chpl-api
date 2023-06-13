package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.datadog.api.client.v1.model.SyntheticsTestDetails;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DatadogChplSynchonizer {
    private DatadogSyntheticTestService datadogSyntheticTestService;
    private ServiceBasedUrlGatherer serviceBasedUrlGatherer;

    public DatadogChplSynchonizer(DatadogSyntheticTestService datadogSyntheticTestService, ServiceBasedUrlGatherer serviceBasedUrlGatherer) {
        this.datadogSyntheticTestService = datadogSyntheticTestService;
        this.serviceBasedUrlGatherer = serviceBasedUrlGatherer;
    }

    public void synchronizeServiceBasedUrlAndDatadogApiTests() {
        List<ServiceBasedUrl> serviceBasedUrls = serviceBasedUrlGatherer.getAllServiceBasedUrls();
        List<SyntheticsTestDetails> syntheticsTestDetails = datadogSyntheticTestService.getAllSyntheticTests();

        addMissingSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
        removeUnusedSyntheticApiTests(serviceBasedUrls, syntheticsTestDetails);
    }

    private void addMissingSyntheticApiTests(List<ServiceBasedUrl> serviceBasedUrls, List<SyntheticsTestDetails> syntheticsTestDetails) {
        List<ServiceBasedUrl> urlsNotInDatadog = new ArrayList<ServiceBasedUrl>(serviceBasedUrls);
        urlsNotInDatadog.removeIf(sbu -> syntheticsTestDetails.stream()
                .filter(synthTest -> synthTest.getConfig().getRequest().getUrl().equals(sbu.getUrl()))
                .findAny()
                .isPresent());

        urlsNotInDatadog.stream()
                .peek(sbu -> LOGGER.info("Adding the following URL to Datadog: {}", sbu.getUrl()))
                .forEach(sbu -> datadogSyntheticTestService.createSyntheticApiTest(sbu.getUrl()));
    }

    private void removeUnusedSyntheticApiTests(List<ServiceBasedUrl> serviceBasedUrls, List<SyntheticsTestDetails> syntheticsTestDetails) {
        List<SyntheticsTestDetails> syntheticsTestDetailsNotUsed = new ArrayList<SyntheticsTestDetails>(syntheticsTestDetails);

        syntheticsTestDetailsNotUsed.removeIf(std -> serviceBasedUrls.stream()
                .filter(serviceBasedUrl -> serviceBasedUrl.getUrl().equals(std.getConfig().getRequest().getUrl()))
                .findAny()
                .isPresent());

        syntheticsTestDetailsNotUsed.stream()
                .peek(synthTestDet -> LOGGER.info("Removing the following URL from Datadog: {}", synthTestDet.getConfig().getRequest().getUrl()))
                .forEach(synthTestDet -> datadogSyntheticTestService.deleteSyntheticApiTest(List.of(synthTestDet.getPublicId())));
    }
}
