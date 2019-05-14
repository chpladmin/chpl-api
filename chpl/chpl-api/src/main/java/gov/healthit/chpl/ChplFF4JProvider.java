package gov.healthit.chpl;

import org.ff4j.FF4j;
import org.ff4j.web.FF4jProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component
public class ChplFF4JProvider implements FF4jProvider {

    @Autowired
    private FF4j ff4j;

    public ChplFF4JProvider() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public FF4j getFF4j() {
        return ff4j;
    }

}
