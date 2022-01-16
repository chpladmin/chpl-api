package gov.healthit.chpl;

import org.ff4j.FF4j;
import org.ff4j.web.FF4jProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChplFF4JProvider implements FF4jProvider {

    @Autowired
    private FF4j ff4j;

    @Override
    public FF4j getFF4j() {
        if (ff4j == null) {
            ff4j = SpringContext.getBean(FF4j.class);
        }
        return ff4j;
    }
}
