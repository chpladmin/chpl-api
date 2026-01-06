package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

@Log4j2
@Component
public class DeprecatedResponseFieldAnnotationIntrospector extends JacksonAnnotationIntrospector {
    private static final long serialVersionUID = 7316344670464634840L;

    @Autowired
    private Environment env;

    @Override
    public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
        Boolean returnDeprecatedFields = Boolean.valueOf(env.getProperty("response.returnDeprecatedFields"));
        LOGGER.info("Return deprecated fields??: " + returnDeprecatedFields);
        if (_findAnnotation(m, JsonIgnore.class) != null) {
            return true;
        } else {
            return _findAnnotation(m, DeprecatedResponseField.class) != null && !returnDeprecatedFields;
        }
    }
}
