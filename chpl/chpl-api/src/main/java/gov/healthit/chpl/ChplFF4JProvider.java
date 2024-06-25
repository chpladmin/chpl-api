package gov.healthit.chpl;

import org.ff4j.FF4j;
import org.ff4j.web.FF4jProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class ChplFF4JProvider implements FF4jProvider {

    private static final ChplFF4JProvider INSTANCE = new ChplFF4JProvider();

    @Autowired
    private FF4j ff4j;

    private ChplFF4JProvider() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public FF4j getFF4j() {
        return ff4j;
    }

    public static ChplFF4JProvider getInstance() {
        return INSTANCE;
    }
}
