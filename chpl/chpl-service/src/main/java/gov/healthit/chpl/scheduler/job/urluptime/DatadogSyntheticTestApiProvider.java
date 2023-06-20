package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datadog.api.client.ApiClient;
import com.datadog.api.client.v1.api.SyntheticsApi;

import lombok.Getter;

@Component
public class DatadogSyntheticTestApiProvider {
    private String datadogApiKey;
    private String datadogAppKey;

    @Getter
    private SyntheticsApi apiInstance = null;

    public DatadogSyntheticTestApiProvider(@Value("${datadog.apiKey}") String datadogApiKey, @Value("${datadog.appKey}") String datadogAppKey) {
        this.datadogApiKey = datadogApiKey;
        this.datadogAppKey = datadogAppKey;

        ApiClient defaultClient = ApiClient.getDefaultApiClient();
        defaultClient.configureApiKeys(getDatadogSecrets());
        apiInstance = new SyntheticsApi(defaultClient);
    }

    private HashMap<String, String> getDatadogSecrets() {
        HashMap<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", datadogApiKey);
        secrets.put("appKeyAuth", datadogAppKey);

        return secrets;
    }

}
