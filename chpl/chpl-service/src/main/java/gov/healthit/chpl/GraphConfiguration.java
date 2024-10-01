package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public GraphServiceClient getGraphServiceClient() {
        ClientSecretCredential clientSecretCredential = null;
        GraphServiceClient graphServiceClient = null;

        LOGGER.debug("Creating a new ClientSecretCredentialBuilder");

        clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(env.getProperty("azure.clientId"))
                .tenantId(env.getProperty("azure.tenantId"))
                .clientSecret(env.getProperty("azure.clientSecret"))
                .build();

        graphServiceClient = new GraphServiceClient(clientSecretCredential, GRAPH_DEFAULT_SCOPE);

        return graphServiceClient;
    }
}
