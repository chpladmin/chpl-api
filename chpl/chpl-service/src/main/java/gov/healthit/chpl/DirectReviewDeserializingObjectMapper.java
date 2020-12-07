package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

@Component
public class DirectReviewDeserializingObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = 8512096693958255305L;

    public DirectReviewDeserializingObjectMapper() {
        super();
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    @Autowired
    public Object setHandlerInstantiator(HandlerInstantiator hi) {
        super.setHandlerInstantiator(hi);
        return this;
    }
}
