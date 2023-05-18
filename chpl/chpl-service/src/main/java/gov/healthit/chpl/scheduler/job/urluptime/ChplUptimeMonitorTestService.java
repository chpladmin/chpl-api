package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.SyntheticsApi;
import com.datadog.api.client.v1.api.SyntheticsApi.GetAPITestLatestResultsOptionalParameters;
import com.datadog.api.client.v1.model.SyntheticsAPITestResultShort;
import com.datadog.api.client.v1.model.SyntheticsGetAPITestLatestResultsResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChplUptimeMonitorTestService {
    private ChplUptimeMonitorDAO chplUptimeMonitorDAO;
    private ChplUptimeMonitorTestDAO chplUptimeMonitorTestDAO;

    @Autowired
    public ChplUptimeMonitorTestService(ChplUptimeMonitorDAO chplUptimeMonitorDAO, ChplUptimeMonitorTestDAO chplUptimeMonitorTestDAO) {
        this.chplUptimeMonitorDAO = chplUptimeMonitorDAO;
        this.chplUptimeMonitorTestDAO = chplUptimeMonitorTestDAO;
    }

    @Transactional
    public void retrieveTestResultsForPreviousDay(SyntheticsApi apiInstance) {
        getAllChplUptimeMonitors().stream()
                .forEach(monitor -> getResultsForTest(monitor.getDatadogMonitorKey(), apiInstance).stream()
                        .forEach(synthTestResult -> chplUptimeMonitorTestDAO.create(ChplUptimeMonitorTest.builder()
                                .chplUptimeMonitorId(monitor.getId())
                                .datadogTestKey(synthTestResult.getResultId())
                                .checkTime(toLocalDateTime(synthTestResult.getCheckTime().longValue()))
                                .passed(synthTestResult.getResult().getPassed())
                                .build())));
    }

    private List<SyntheticsAPITestResultShort> getResultsForTest(String publicTestKey, SyntheticsApi apiInstance) {
        GetAPITestLatestResultsOptionalParameters params = new GetAPITestLatestResultsOptionalParameters();
        ZonedDateTime morning = ZonedDateTime.now(ZoneId.of("US/Eastern")).withHour(4).withMinute(0).minusDays(1);
        ZonedDateTime evening = ZonedDateTime.now(ZoneId.of("US/Eastern")).withHour(16).withMinute(0).minusDays(1);


        params.fromTs(morning.toInstant().toEpochMilli());
        params.toTs(evening.toInstant().toEpochMilli());
        params.probeDc(List.of("azure:eastus"));

        SyntheticsGetAPITestLatestResultsResponse response;
        List<SyntheticsAPITestResultShort> testResults = new ArrayList<SyntheticsAPITestResultShort>();
        try {

            response = apiInstance.getAPITestLatestResults(publicTestKey, params);

            while (response.getResults().size() > 1) {
                testResults.addAll(response.getResults());
                Long ts = getMostRecentTimestamp(response.getResults());
                //LOGGER.info(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("US/Eastern")).format(formatter));
                params.fromTs(ts);
                response = apiInstance.getAPITestLatestResults(publicTestKey, params);
            }
        } catch (ApiException e) {
            response = null;
            LOGGER.error("Could not retrieve results for test key: {}", publicTestKey, e);
        }
        return testResults;
    }

    private Long getMostRecentTimestamp(List<SyntheticsAPITestResultShort> testResults) {
        return testResults.stream()
                .mapToLong(result -> Math.round(result.getCheckTime()))
                .max()
                .getAsLong();
    }


    private List<ChplUptimeMonitor> getAllChplUptimeMonitors() {
        return chplUptimeMonitorDAO.getAll();
    }

    private LocalDateTime toLocalDateTime(Long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts),
                TimeZone.getDefault().toZoneId());
    }
}
