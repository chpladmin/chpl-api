package gov.healthit.chpl;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.google.common.base.Predicate;

import springfox.documentation.PathProvider;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@PropertySource("classpath:environment.properties")
@EnableSwagger2
public class SwaggerConfig implements EnvironmentAware {

    private static final Logger LOGGER = LogManager.getLogger(SwaggerConfig.class);

    @Autowired
    private ServletContext context;
    @Autowired
    private Environment env;

    @Override
    public void setEnvironment(final Environment environment) {
        LOGGER.info("setEnvironment");
        this.env = environment;
    }

    @Bean
    public Docket customDocket() {
        LOGGER.info("get Docket");
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).pathProvider(pathProvider()).select()
                .paths(this.paths()).build();
    }

    private ApiInfo apiInfo() {
        LOGGER.info("get ApiInfo");
        return new ApiInfo("CHPL", "Certified Health IT Product Listing", "15.4.0", "http://terms/of/service.url",
                "CHPL@ainq.com", "License Text", "https://github.com/chpladmin/chpl-api/blob/staging/LICENSE");
    }

    private PathProvider pathProvider() {
        LOGGER.info("get PathProvider");
        return new AbsolutePathProvider(context);
    }

    @SuppressWarnings("unchecked")
    private Predicate<String> paths() {
        LOGGER.info("get Predicate paths");
        return or(regex("/acbs.*"), regex("/activity.*"), regex("/announcements.*"), regex("/atls.*"), regex("/auth.*"),
                regex("/certification_ids.*"), regex("/certified_products.*"), regex("/certified_product_details.*"),
                regex("/collections.*"), regex("/corrective_action_plan.*"), regex("/data/.*"), regex("/download.*"),
                regex("/jobs.*"), regex("/key.*"), regex("/notifications.*"), regex("/products.*"), regex("/search.*"),
                regex("/surveillance.*"), regex("/status"), regex("/cache_status"), regex("/users.*"),
                regex("/developers.*"), regex("/versions.*"), regex("/decertifications/.*"));
    }

    private class AbsolutePathProvider extends RelativePathProvider {
        AbsolutePathProvider(final ServletContext localContext) {
            super(localContext);
        }

        @Override
        public String getApplicationBasePath() {
            LOGGER.info("get application base path");
            return env.getProperty("basePath");
        }
    }

    public ServletContext getContext() {
        return context;
    }

    public void setContext(final ServletContext context) {
        this.context = context;
    }
}
