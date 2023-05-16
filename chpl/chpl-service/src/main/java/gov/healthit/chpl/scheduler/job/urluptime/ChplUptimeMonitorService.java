package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.SyntheticsApi;
import com.datadog.api.client.v1.model.SyntheticsListTestsResponse;
import com.datadog.api.client.v1.model.SyntheticsTestDetails;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChplUptimeMonitorService {
    private ChplUptimeMonitorDAO chplUptimeMonitorDAO;

    @Autowired
    public ChplUptimeMonitorService(ChplUptimeMonitorDAO chplUptimeMonitorDAO) {
        this.chplUptimeMonitorDAO = chplUptimeMonitorDAO;
    }

    @Transactional
    public void go(SyntheticsApi apiInstance) {
        try {
            addChplUptimeMonitors(missingFromDb(getAllChplUptimeMonitors(), getAllSyntheticTests(apiInstance).getTests()));
        } catch (ApiException e) {
            LOGGER.error("Error retrieving Synthetic Test Details: {}", e.getMessage(), e);
        }
    }

    private void addChplUptimeMonitors(List<SyntheticsTestDetails> syntheticsTestDetails) {
        syntheticsTestDetails.stream()
                .map(synth -> ChplUptimeMonitor.builder()
                        .description(synth.getName())
                        .url(synth.getConfig().getRequest().getUrl())
                        .datadogMonitorKey(synth.getPublicId())
                        .build())
                .forEach(monitor -> chplUptimeMonitorDAO.create(monitor));
    }

    private List<SyntheticsTestDetails> missingFromDb(List<ChplUptimeMonitor> chplUptimeMonitors, List<SyntheticsTestDetails> syntheticsTestDetails) {
        return syntheticsTestDetails.stream()
                .filter(synth -> !contains(chplUptimeMonitors, synth))
                .toList();
    }

    private SyntheticsListTestsResponse getAllSyntheticTests(SyntheticsApi apiInstance) throws ApiException {
        return apiInstance.listTests();
    }

    private List<ChplUptimeMonitor> getAllChplUptimeMonitors() {
        return chplUptimeMonitorDAO.getAll();
    }

    private Boolean contains(List<ChplUptimeMonitor> chplUptimeMonitors, SyntheticsTestDetails syntheticsTestDetail) {
        if (CollectionUtils.isEmpty(chplUptimeMonitors)) {
            return false;
        }
        return chplUptimeMonitors.stream()
                .filter(chplUptimeMonitor -> chplUptimeMonitor.getDatadogMonitorKey().equals(syntheticsTestDetail.getPublicId()))
                .findAny()
                .isPresent();
    }
}
