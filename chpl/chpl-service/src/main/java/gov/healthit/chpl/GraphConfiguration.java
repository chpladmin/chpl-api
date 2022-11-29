package gov.healthit.chpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

import lombok.extern.log4j.Log4j2;
import okhttp3.Request;

@Configuration
@Log4j2
public class GraphConfiguration {
    private static final String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";

    @Autowired
    private Environment env;

    @Bean
    public GraphServiceClient<Request> getGraphServiceClient() {
        ClientSecretCredential clientSecretCredential = null;
        GraphServiceClient<Request> graphServiceClient = null;

        LOGGER.debug("Creating a new ClientSecretCredentialBuilder");
        clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(env.getProperty("azure.clientId"))
            .tenantId(env.getProperty("azure.tenantId"))
            .clientSecret(env.getProperty("azure.clientSecret"))
            .build();

        LOGGER.debug("Creating a new GraphServiceClient");
        final TokenCredentialAuthProvider authProvider =
            new TokenCredentialAuthProvider(
                List.of(GRAPH_DEFAULT_SCOPE), clientSecretCredential);

        graphServiceClient = GraphServiceClient.builder()
            .authenticationProvider(authProvider)
            .buildClient();

        return graphServiceClient;
    }
}
