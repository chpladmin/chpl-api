package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.SyntheticsApi.GetAPITestLatestResultsOptionalParameters;
import com.datadog.api.client.v1.model.SyntheticsAPITestResultShort;
import com.datadog.api.client.v1.model.SyntheticsGetAPITestLatestResultsResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2()
@Component
public class SyntheticTestResultService {

    private SyntheticTestApiProvider apiProvider;

    @Autowired
    public SyntheticTestResultService(SyntheticTestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    public List<SyntheticsAPITestResultShort> getSyntheticTestResults(String publicTestKey, LocalDate dateToGetResultsFor) {
        GetAPITestLatestResultsOptionalParameters params = new GetAPITestLatestResultsOptionalParameters();
        ZonedDateTime startTime = dateToGetResultsFor.atTime(LocalTime.MIN).atZone(ZoneId.of("America/New_York"));
        ZonedDateTime endTime = dateToGetResultsFor.atTime(LocalTime.MAX).atZone(ZoneId.of("America/New_York"));

        params.fromTs(startTime.toInstant().toEpochMilli());
        params.toTs(endTime.toInstant().toEpochMilli());

        LOGGER.info("Retrieving tests for {} between {} and {}", publicTestKey, startTime, endTime);
        SyntheticsGetAPITestLatestResultsResponse response;
        List<SyntheticsAPITestResultShort> testResults = new ArrayList<SyntheticsAPITestResultShort>();
        try {
            response = apiProvider.getApiInstance().getAPITestLatestResults(publicTestKey, params);
            while (response.getResults().size() > 1) {
                testResults.addAll(response.getResults());
                Long ts = getMostRecentTimestamp(response.getResults());
                params.fromTs(ts);
                response = apiProvider.getApiInstance().getAPITestLatestResults(publicTestKey, params);
            }
            LOGGER.info("Found {} tests for monitor {}", testResults.size(), publicTestKey);

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
}
