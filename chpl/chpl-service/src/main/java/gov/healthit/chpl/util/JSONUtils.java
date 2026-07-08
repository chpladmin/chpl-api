package gov.healthit.chpl.util;

import java.io.IOException;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flipkart.zjsonpatch.Jackson3JsonDiff;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.activity.ActivityExclude;
import gov.healthit.chpl.sed.DeprecatedSedSummaryData;
import gov.healthit.chpl.sed.DeprecatedSedTestTaskData;
import gov.healthit.chpl.sed.DeprecatedUcdData;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;

@Component
@Log4j2
public final class JSONUtils {
    private ObjectReader reader;
    private ObjectWriter writer;
    private JsonMapper mapperExcludingIgnoredFields;
    private ObjectWriter writerExcludingIgnoredFields;

    @Autowired
    public JSONUtils(JsonMapper jsonMapper,
            FF4j ff4j) {
        this.reader = jsonMapper.reader();
        this.writer = jsonMapper.writer();
        this.mapperExcludingIgnoredFields = JsonMapper.builder()
                .annotationIntrospector(new JacksonAnnotationIntrospector() {
                    private static final long serialVersionUID = -1856550954546461022L;

                    @Override
                    public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
                        return super.hasIgnoreMarker(config, m)
                                || m.hasAnnotation(Deprecated.class)
                                || m.hasAnnotation(ActivityExclude.class)
                                || isSedAndIgnorable(m);
                    }

                    private boolean isSedAndIgnorable(AnnotatedMember m) {
                        boolean isHti5Erd = ff4j.check(FeatureList.HTI_5_ERD);
                        boolean isSedSummary = _findAnnotation(m, DeprecatedSedSummaryData.class) != null;
                        boolean isSedTestTask = _findAnnotation(m, DeprecatedSedTestTaskData.class) != null;
                        boolean isSedUcdData = _findAnnotation(m, DeprecatedUcdData.class) != null;
                        return isHti5Erd && (isSedSummary || isSedTestTask || isSedUcdData);
                    }
                  })
                .build();
        this.writerExcludingIgnoredFields = mapperExcludingIgnoredFields.writer();
    }

    public ObjectReader getReader() {
        return reader;
    }

    public ObjectWriter getWriter() {
        return writer;
    }

    public String toJSON(final Object obj) throws JacksonException {

        String json = null;
        if (obj != null) {
            json = getWriter().writeValueAsString(obj);
        }
        return json;
    }

    public String toJSONExcludingIgnoredFields(final Object obj) throws JacksonException {
        String json = null;
        if (obj != null) {
            json = writerExcludingIgnoredFields.writeValueAsString(obj);
        }
        return json;
    }

    public <T> T fromJSON(final String json, final Class<T> type)
            throws JacksonException, IOException {

        JsonNode node = getReader().readTree(json);
        T obj = getReader().treeToValue(node, type);
        return obj;

    }

    public boolean jsonEquals(String json1, String json2)
            throws JacksonException, IOException {
        if (json1 == null && json2 == null) {
            return true;
        } else if ((json1 == null && json2 != null)
                || (json1 != null && json2 == null)) {
            return false;
        }

        Boolean equals;
        try {
            JsonNode node1 = getReader().readTree(json1);
            JsonNode node2 = getReader().readTree(json2);
            equals = node1.equals(node2);

            JsonNode patch = Jackson3JsonDiff.asJson(node1, node2);
            if (patch != null && !patch.isEmpty()) {
                LOGGER.debug("Data was updated. Differences found in the JSON.");
                LOGGER.debug(patch.toString());
            }
        } catch (final NullPointerException e) {
            equals = false;
        }
        return equals;
    }

}
