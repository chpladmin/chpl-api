package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datadog.api.client.ApiClient;
import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.SyntheticsApi;
import com.datadog.api.client.v1.model.SyntheticsTestDetails;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DatadogSyntheticTestService {

    private String datadogApiKey;
    private String datadogAppKey;
    private SyntheticsApi apiInstance = null;

    public DatadogSyntheticTestService(@Value("${datadog.apiKey}") String datadogApiKey, @Value("${datadog.apiKey}") String datadogAppKey) {
        ApiClient defaultClient = ApiClient.getDefaultApiClient();
        defaultClient.configureApiKeys(getDatadogSecrets());
        apiInstance = new SyntheticsApi(defaultClient);
    }

    public List<SyntheticsTestDetails> getAllSyntheticTests() {
        try {
            return apiInstance.listTests().getTests();
        } catch (ApiException e) {
            LOGGER.error("Could not retrieve Synthetic Tests from Datadog", e);
            return null;
        }
    }

    private HashMap<String, String> getDatadogSecrets() {
        HashMap<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", datadogApiKey);
        secrets.put("appKeyAuth", datadogAppKey);
        return secrets;
    }
}
