package gov.healthit.chpl.scheduler.job.urluptime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2(topic =  "urlUptimeCreatorJobLogger")
@Component
public class DatadogMonitorTestService {
    private DatadogMonitorDAO datadogMonitorDAO;
    private DatadogMonitorTestDAO datadogMonitorTestDAO;

    @Autowired
    public DatadogMonitorTestService(DatadogMonitorDAO datadogMonitorDAO, DatadogMonitorTestDAO datadogMonitorTestDAO) {
        this.datadogMonitorDAO = datadogMonitorDAO;
        this.datadogMonitorTestDAO = datadogMonitorTestDAO;
    }

//    @Transactional
//    public void retrieveTestResultsForPreviousDay(SyntheticsApi apiInstance) {
//        LOGGER.info("Adding Datadog Monitor Tests to CHPL");
//        getAllChplUptimeMonitors().stream()
//                .forEach(monitor -> getResultsForTest(monitor.getDatadogMonitorKey(), apiInstance).stream()
//                        .forEach(synthTestResult -> chplUptimeMonitorTestDAO.create(ChplUptimeMonitorTest.builder()
//                                .chplUptimeMonitorId(monitor.getId())
//                                .datadogTestKey(synthTestResult.getResultId())
//                                .checkTime(toLocalDateTime(synthTestResult.getCheckTime().longValue()))
//                                .passed(synthTestResult.getResult().getPassed())
//                                .build())));
//        LOGGER.info("Completed Adding Datadog Monitor Tests to CHPL");
//    }
//
//    private List<SyntheticsAPITestResultShort> getResultsForTest(String publicTestKey, SyntheticsApi apiInstance) {
//        GetAPITestLatestResultsOptionalParameters params = new GetAPITestLatestResultsOptionalParameters();
//        ZonedDateTime morning = ZonedDateTime.now(ZoneId.of("US/Eastern")).withHour(4).withMinute(0).minusDays(1);
//        ZonedDateTime evening = ZonedDateTime.now(ZoneId.of("US/Eastern")).withHour(16).withMinute(0).minusDays(1);
//
//
//        params.fromTs(morning.toInstant().toEpochMilli());
//        params.toTs(evening.toInstant().toEpochMilli());
//        params.probeDc(List.of("azure:eastus"));
//
//        LOGGER.info("Retrieving tests for {} between {} and {}", publicTestKey, morning, evening);
//        SyntheticsGetAPITestLatestResultsResponse response;
//        List<SyntheticsAPITestResultShort> testResults = new ArrayList<SyntheticsAPITestResultShort>();
//        try {
//
//            response = apiInstance.getAPITestLatestResults(publicTestKey, params);
//
//            while (response.getResults().size() > 1) {
//                testResults.addAll(response.getResults());
//                Long ts = getMostRecentTimestamp(response.getResults());
//                params.fromTs(ts);
//                response = apiInstance.getAPITestLatestResults(publicTestKey, params);
//            }
//            LOGGER.info("Found {} tests for monitor {}", testResults.size(), publicTestKey);
//
//        } catch (ApiException e) {
//            response = null;
//            LOGGER.error("Could not retrieve results for test key: {}", publicTestKey, e);
//        }
//
//        return testResults;
//    }
//
//    private Long getMostRecentTimestamp(List<SyntheticsAPITestResultShort> testResults) {
//        return testResults.stream()
//                .mapToLong(result -> Math.round(result.getCheckTime()))
//                .max()
//                .getAsLong();
//    }
//
//
//    private List<ChplUptimeMonitor> getAllChplUptimeMonitors() {
//        return chplUptimeMonitorDAO.getAll();
//    }
//
//    private LocalDateTime toLocalDateTime(Long ts) {
//        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts),
//                TimeZone.getDefault().toZoneId());
//    }
}
