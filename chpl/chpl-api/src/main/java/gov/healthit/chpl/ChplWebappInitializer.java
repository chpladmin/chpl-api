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
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("gov.healthit.chpl");

        container.addListener(new ContextLoaderListener(context));
        container.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));

        ServletRegistration.Dynamic dispatcher = container.addServlet("rest", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
    }
}
