package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datadog.api.client.ApiClient;
import com.datadog.api.client.v1.api.SyntheticsApi;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBaseUrlListUptimeCreatorJobLogger")
@Component
public class DatadogSyntheticsTestApiProvider {
    private String datadogApiKey;
    private String datadogAppKey;

    @Getter
    private SyntheticsApi apiInstance = null;

    public DatadogSyntheticsTestApiProvider(@Value("${datadog.apiKey}") String datadogApiKey, @Value("${datadog.appKey}") String datadogAppKey) {
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
