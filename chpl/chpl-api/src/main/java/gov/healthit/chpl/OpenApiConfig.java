package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${api.version}") String apiVersion) {
        return new OpenAPI().info(new Info().title("CHPL API")
            .version(apiVersion)
            .description("This is the CHPL's open Restful API documentation.")
            .termsOfService("http://swagger.io/terms/")
            .license(new License().name("Apache 2.0")
                .url("http://springdoc.org")));
    }
}
