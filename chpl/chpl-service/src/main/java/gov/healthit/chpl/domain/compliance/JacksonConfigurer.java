package gov.healthit.chpl.domain.compliance;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import gov.healthit.chpl.DirectReviewDeserializingObjectMapper;

@Component
public class JacksonConfigurer {
    private RequestMappingHandlerAdapter requestMethodHandlerAdapter;
    private DirectReviewDeserializingObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        List<HttpMessageConverter<?>> messageConverters = requestMethodHandlerAdapter.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter m = (MappingJackson2HttpMessageConverter) messageConverter;
                m.setObjectMapper(objectMapper);
            }
        }
    }

    @Autowired
    public void setRequestMappingHandlerAdapter(RequestMappingHandlerAdapter obj) {
        this.requestMethodHandlerAdapter  = obj;
    }

    @Autowired
    public void setObjectMapper(DirectReviewDeserializingObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
