package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class GraphConfiguration {
    private static final String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";

    @Autowired
    private Environment env;

    //When the feature ONC_TO_ASTP_EMAIL is removed the @Scope annotation is no longer needed.
    //It is here so that each time the GraphServiceClient bean is requested the flag will be checked
    //to get the correct email configuration. When that no longer needs to be able to be switched between
    //ONC and ASTP then we can remove the Prototype Scope.
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    public GraphServiceClient getGraphServiceClient() {
        ClientSecretCredential clientSecretCredential = null;
        GraphServiceClient graphServiceClient = null;

        LOGGER.info("Creating a new ClientSecretCredentialBuilder");

        clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(getClientId())
                .tenantId(getTenantId())
                .clientSecret(getClientSecret())
                .build();

        graphServiceClient = new GraphServiceClient(clientSecretCredential, GRAPH_DEFAULT_SCOPE);
        return graphServiceClient;
    }

    private String getClientId() {
        return env.getProperty("azure.clientId.onc");
    }

    private String getTenantId() {
        return env.getProperty("azure.tenantId.onc");
    }

    private String getClientSecret() {
        return env.getProperty("azure.clientSecret.onc");
    }
}
