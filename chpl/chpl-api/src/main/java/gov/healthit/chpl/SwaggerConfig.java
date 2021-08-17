package gov.healthit.chpl;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import com.google.common.base.Predicate;

import springfox.documentation.PathProvider;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration class for swagger documentation.
 * @author kekey
 *
 */
@Configuration
@PropertySources({
    @PropertySource("classpath:/environment.properties"),
    @PropertySource(value = "classpath:/environment-override.properties", ignoreResourceNotFound = true)
})
@EnableSwagger2
public class SwaggerConfig implements EnvironmentAware {

    @Autowired
    private ServletContext context;
    @Autowired
    private Environment env;

    @Override
    public void setEnvironment(final Environment environment) {
        this.env = environment;
    }

    @Bean
    public Docket customDocket() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).pathProvider(pathProvider()).select()
                .paths(this.paths()).build();
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("CHPL Development Team. "
                + "Please submit any questions using the Health IT Feedback Form and "
                + "select the \"Certified Health IT Products List (CHPL)\" category.",
                "https://inquiry.healthit.gov/support/plugins/servlet/loginfreeRedirMain?portalid=2&request=51",
                "");
        return new ApiInfo("CHPL", "Certified Health IT Product Listing", "30.5.0", "",
                contact, "License Text", "https://github.com/chpladmin/chpl-api/blob/staging/LICENSE");
    }

    private PathProvider pathProvider() {
        return new AbsolutePathProvider(context);
    }

    @SuppressWarnings("unchecked")
    private Predicate<String> paths() {
        return or(regex("/acbs.*"), regex("/activity.*"), regex("/announcements.*"), regex("/atls.*"), regex("/auth.*"),
                regex("/certification_ids.*"), regex("/certified_products.*"), regex("/certified_product_details.*"),
                regex("/collections.*"), regex("/data/.*"), regex("/download.*"), regex("/files.*"), regex("/jobs.*"),
                regex("/key.*"), regex("/meaningful_use.*"), regex("/products.*"), regex("/promoting-interoperability.*"),
                regex("/search.*"), regex("/surveillance.*"), regex("/surveillance-report.*"),
                regex("/status"), regex("/cache_status"), regex("/system-status"),
                regex("/users.*"), regex("/developers.*"), regex("/versions.*"), regex("/decertifications/.*"),
                regex("/schedules.*"), regex("/complaints.*"), regex("/change-requests.*"), regex("/svaps.*"));
    }

    private class AbsolutePathProvider extends RelativePathProvider {
        AbsolutePathProvider(final ServletContext localContext) {
            super(localContext);
        }

        @Override
        public String getApplicationBasePath() {
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
