package gov.healthit.chpl;

import java.util.EnumSet;

import org.springframework.core.env.Environment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionTrackingMode;

public class ChplWebappInitializer implements WebApplicationInitializer {
    private static final long MAX_UPLOAD_FILE_SIZE = 5242880;

    @Override
    public void onStartup(ServletContext container) {
        AnnotationConfigWebApplicationContext context = getContext();
        Environment environment = context.getEnvironment();

        container.addListener(new ContextLoaderListener(context));
        container.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        ServletRegistration.Dynamic dispatcher = container.addServlet("rest", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        dispatcher.setMultipartConfig(new MultipartConfigElement(
                environment.getProperty("downloadFolderPath"),
                MAX_UPLOAD_FILE_SIZE,
                MAX_UPLOAD_FILE_SIZE,
                0));
    }

    private AnnotationConfigWebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(CHPLConfig.class, CHPLHttpSecurityConfig.class);
        //the below classes are needed for OpenAPI to work outside of a spring-boot environment
        context.register(org.springdoc.core.properties.SwaggerUiConfigProperties.class,
              org.springdoc.core.properties.SwaggerUiOAuthProperties.class,
              org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
              org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration.class,
              org.springdoc.core.configuration.SpringDocConfiguration.class,
              org.springdoc.core.properties.SpringDocConfigProperties.class,
              org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class);

        return context;
     }
}
