package gov.healthit.chpl;

import java.util.EnumSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionTrackingMode;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class ChplWebappInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) {
        AnnotationConfigWebApplicationContext context = getContext();
        container.addListener(new ContextLoaderListener(context));
        container.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        ServletRegistration.Dynamic dispatcher = container.addServlet("rest", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
    }

    private AnnotationConfigWebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(CHPLConfig.class, CHPLHttpSecurityConfig.class);
        //the below classes are needed for OpenAPI to work outside of a spring-boot environment
        context.register(org.springdoc.core.SwaggerUiConfigProperties.class,
              org.springdoc.core.SwaggerUiOAuthProperties.class,
              org.springdoc.webmvc.core.SpringDocWebMvcConfiguration.class,
              org.springdoc.webmvc.core.MultipleOpenApiSupportConfiguration.class,
              org.springdoc.core.SpringDocConfiguration.class, org.springdoc.core.SpringDocConfigProperties.class,
              org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class);

        return context;
     }
}
