package gov.healthit.chpl.domain.compliance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

@Component
public class DirectReviewDeserializingObjectMapper extends ObjectMapper {

    @Autowired
    ApplicationContext applicationContext;

    public DirectReviewDeserializingObjectMapper() {
        // Jackson confused by what to set or by extra properties?  Fix it.
        this.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        this.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    @Autowired
    public void setHandlerInstantiator(HandlerInstantiator hi) {
        super.setHandlerInstantiator(hi);
    }
}
