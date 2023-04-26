package gov.healthit.chpl.web.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.datadog.api.client.ApiClient;
import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.SyntheticsApi;
import com.datadog.api.client.v1.api.SyntheticsApi.GetAPITestLatestResultsOptionalParameters;
import com.datadog.api.client.v1.model.SyntheticsAPITest;
import com.datadog.api.client.v1.model.SyntheticsAPITestConfig;
import com.datadog.api.client.v1.model.SyntheticsAPITestType;
import com.datadog.api.client.v1.model.SyntheticsAssertion;
import com.datadog.api.client.v1.model.SyntheticsAssertionOperator;
import com.datadog.api.client.v1.model.SyntheticsAssertionTarget;
import com.datadog.api.client.v1.model.SyntheticsAssertionType;
import com.datadog.api.client.v1.model.SyntheticsGetAPITestLatestResultsResponse;
import com.datadog.api.client.v1.model.SyntheticsTestOptions;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsMonitorOptions;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsScheduling;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsSchedulingTimeframe;
import com.datadog.api.client.v1.model.SyntheticsTestRequest;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "datadog", description = "Allows integration with Datadog")
@RestController
@RequestMapping("/datadog")
public class DatadogController {


    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody SyntheticsGetAPITestLatestResultsResponse  getDatadog() {

        HashMap<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", "");
        secrets.put("appKeyAuth", "");


        ApiClient defaultClient = ApiClient.getDefaultApiClient();
        defaultClient.configureApiKeys(secrets);
        SyntheticsApi apiInstance = new SyntheticsApi(defaultClient);

        try {
            //return apiInstance.listTests();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minusDays(7);

            GetAPITestLatestResultsOptionalParameters x = new GetAPITestLatestResultsOptionalParameters();
            //x.fromTs(weekAgo.toInstant(ZoneOffset.UTC).toEpochMilli());
            //x.toTs(weekAgo.toInstant(ZoneOffset.UTC).toEpochMilli() + 43200000);
            x.probeDc(List.of("azure:eastus"));


            SyntheticsGetAPITestLatestResultsResponse response = apiInstance.getAPITestLatestResults("zyz-biq-55w", x);

            return response;

          } catch (Exception e) {
              e.printStackTrace();
            }

        return null;
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public void createDatadog() {
        HashMap<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", "");
        secrets.put("appKeyAuth", "");


        ApiClient defaultClient = ApiClient.getDefaultApiClient();
        defaultClient.configureApiKeys(secrets);
        SyntheticsApi apiInstance = new SyntheticsApi(defaultClient);

        SyntheticsAPITest body =
            new SyntheticsAPITest()
                .config(
                    new SyntheticsAPITestConfig()
                        .request(new SyntheticsTestRequest()
                                .method("GET")
                                .url("https://chpl-dev.healthit.gov"))
                        .assertions(List.of(
                                new SyntheticsAssertion(
                                        new SyntheticsAssertionTarget()
                                                .operator(SyntheticsAssertionOperator.LESS_THAN)
                                                .target(5000L)
                                                .type(SyntheticsAssertionType.RESPONSE_TIME)),
                                new SyntheticsAssertion(
                                        new SyntheticsAssertionTarget()
                                                .operator(SyntheticsAssertionOperator.IS)
                                                .target(200)
                                                .type(SyntheticsAssertionType.STATUS_CODE)))))

                .locations(List.of("aws:us-east-2"))
                .name("OCD-4199 - TEST - TEST")
                .message("This is the message for: OCD-4199 - TEST - TEST")
                .type(SyntheticsAPITestType.API)
                .options(new SyntheticsTestOptions()
                    .minFailureDuration(0L)
                    .minLocationFailed(1L)
                    .monitorOptions(new SyntheticsTestOptionsMonitorOptions().renotifyInterval(0L))
                    .monitorName("Monitor: OCD-4199 - TEST - TEST")
                    .tickEvery(3600L)
                    .scheduling(new SyntheticsTestOptionsScheduling()
                            .timezone("America/New_York")
                            .timeframes(List.of(
                                    new SyntheticsTestOptionsSchedulingTimeframe().day(1).from("08:00").to("20:00"),
                                    new SyntheticsTestOptionsSchedulingTimeframe().day(2).from("08:00").to("20:00"),
                                    new SyntheticsTestOptionsSchedulingTimeframe().day(3).from("08:00").to("20:00"),
                                    new SyntheticsTestOptionsSchedulingTimeframe().day(4).from("08:00").to("20:00"),
                                    new SyntheticsTestOptionsSchedulingTimeframe().day(5).from("08:00").to("20:00"),
                                    new SyntheticsTestOptionsSchedulingTimeframe().day(6).from("08:00").to("20:00"),
                                    new SyntheticsTestOptionsSchedulingTimeframe().day(7).from("08:00").to("20:00")))));


        try {
          SyntheticsAPITest result = apiInstance.createSyntheticsAPITest(body);
          System.out.println(result);
        } catch (ApiException e) {
          System.err.println("Exception when calling SyntheticsApi#createSyntheticsAPITest");
          System.err.println("Status code: " + e.getCode());
          System.err.println("Reason: " + e.getResponseBody());
          System.err.println("Response headers: " + e.getResponseHeaders());
          e.printStackTrace();
        }

    }
}
