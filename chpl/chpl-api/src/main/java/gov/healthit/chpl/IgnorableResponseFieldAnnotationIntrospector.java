package gov.healthit.chpl;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.listing.measure.DeprecatedMeasureData;
import gov.healthit.chpl.realworldtesting.DeprecatedRwtPlansData;
import gov.healthit.chpl.sed.DeprecatedSedSummaryData;
import gov.healthit.chpl.sed.DeprecatedSedTestTaskData;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

@Log4j2
@Component
public class IgnorableResponseFieldAnnotationIntrospector extends JacksonAnnotationIntrospector {
    private static final long serialVersionUID = 7316344670464634840L;

    @Autowired
    private Environment env;

    @Autowired
    private FF4j ff4j;

    @Override
    public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
        if (super.hasIgnoreMarker(config, m)) {
            return true;
        } else {
            return isDeprecatedAndIgnorable(m)
                    || isSedAndIgnorable(m)
                    || isRwtAndIgnorable(m)
                    || isMeasuresAndIgnorable(m);
        }
    }

    private boolean isDeprecatedAndIgnorable(AnnotatedMember m) {
        Boolean returnDeprecatedFields = Boolean.valueOf(env.getProperty("response.returnDeprecatedFields"));
        boolean isDeprecated = _findAnnotation(m, DeprecatedResponseField.class) != null;
        return !returnDeprecatedFields && isDeprecated;
    }

    private boolean isSedAndIgnorable(AnnotatedMember m) {
        boolean isFlagOn = ff4j.check(FeatureList.HTI_5_ERD);
        boolean isSedSummary = _findAnnotation(m, DeprecatedSedSummaryData.class) != null;
        boolean isSedTestTask = _findAnnotation(m, DeprecatedSedTestTaskData.class) != null;
        return isFlagOn && (isSedSummary || isSedTestTask);
    }

    private boolean isRwtAndIgnorable(AnnotatedMember m) {
        boolean isFlagOn = ff4j.check(FeatureList.HTI_5_ERD);
        boolean isRwtPlans = _findAnnotation(m, DeprecatedRwtPlansData.class) != null;
        return isFlagOn && isRwtPlans;
    }

    private boolean isMeasuresAndIgnorable(AnnotatedMember m) {
        boolean isFlagOn = ff4j.check(FeatureList.HTI_5_2027_01_01);
        boolean isMeasureField = _findAnnotation(m, DeprecatedMeasureData.class) != null;
        return isFlagOn && isMeasureField;
    }
}
