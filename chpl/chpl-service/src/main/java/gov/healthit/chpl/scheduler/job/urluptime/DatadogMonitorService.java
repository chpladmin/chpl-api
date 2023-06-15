package gov.healthit.chpl.scheduler.job.urluptime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2(topic =  "urlUptimeCreatorJobLogger")
@Component
public class DatadogMonitorService {
    private DatadogMonitorDAO datadogMonitorDAO;

    @Autowired
    public DatadogMonitorService(DatadogMonitorDAO datadogMonitorDAO) {
        this.datadogMonitorDAO = datadogMonitorDAO;
    }

//    @Transactional
//    public void synchronizeDatadogMonitorsForReporting(SyntheticsApi apiInstance) {
//        try {
//            LOGGER.info("Synchronizing Datadog Monitors with CHPL");
//            addChplUptimeMonitors(missingFromDb(getAllChplUptimeMonitors(), getAllSyntheticTests(apiInstance).getTests()));
//            LOGGER.info("Completing Datadog Monitors with CHPL");
//        } catch (ApiException e) {
//            LOGGER.error("Error retrieving Synthetic Test Details: {}", e.getMessage(), e);
//        }
//    }
//
//    private void addChplUptimeMonitors(List<SyntheticsTestDetails> syntheticsTestDetails) {
//        syntheticsTestDetails.stream()
//                .map(synth -> ChplUptimeMonitor.builder()
//                        .description(synth.getName())
//                        .url(synth.getConfig().getRequest().getUrl())
//                        .datadogMonitorKey(synth.getPublicId())
//                        .build())
//                .peek(monitor -> LOGGER.info("Monitor found in Datadog, adding to CHPL: {}", monitor.getUrl()))
//                .forEach(monitor -> chplUptimeMonitorDAO.create(monitor));
//    }
//
//    private List<SyntheticsTestDetails> missingFromDb(List<ChplUptimeMonitor> chplUptimeMonitors, List<SyntheticsTestDetails> syntheticsTestDetails) {
//        return syntheticsTestDetails.stream()
//                .filter(synth -> !contains(chplUptimeMonitors, synth))
//                .toList();
//    }
//
//    private SyntheticsListTestsResponse getAllSyntheticTests(SyntheticsApi apiInstance) throws ApiException {
//        return apiInstance.listTests();
//    }
//
//    private List<ChplUptimeMonitor> getAllChplUptimeMonitors() {
//        return chplUptimeMonitorDAO.getAll();
//    }
//
//    private Boolean contains(List<ChplUptimeMonitor> chplUptimeMonitors, SyntheticsTestDetails syntheticsTestDetail) {
//        if (CollectionUtils.isEmpty(chplUptimeMonitors)) {
//            return false;
//        }
//        return chplUptimeMonitors.stream()
//                .filter(chplUptimeMonitor -> chplUptimeMonitor.getDatadogMonitorKey().equals(syntheticsTestDetail.getPublicId()))
//                .findAny()
//                .isPresent();
//    }
}
