package gov.healthit.chpl.util;

import java.io.IOException;

import com.flipkart.zjsonpatch.Jackson3JsonDiff;

import gov.healthit.chpl.activity.ActivityExclude;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
public final class JSONUtils {

    private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
    private static final ObjectReader READER = MAPPER.reader();
    private static final ObjectWriter WRITER = MAPPER.writer();

    private static final ObjectMapper MAPPER_EXCLUDING_IGNORED_FIELDS = JsonMapper.builder()
            .annotationIntrospector(new JacksonAnnotationIntrospector() {
                private static final long serialVersionUID = -1856550954546461022L;

                @Override
                public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
                    return super.hasIgnoreMarker(config, m)
                            || m.hasAnnotation(Deprecated.class)
                            || m.hasAnnotation(ActivityExclude.class);
                }
              })
            .build();
    private static final ObjectWriter WRITER_EXCLUDING_IGNORED_FIELDS = MAPPER_EXCLUDING_IGNORED_FIELDS.writer();

    private JSONUtils() {
    }

    public static ObjectReader getReader() {
        return READER;
    }

    public static ObjectWriter getWriter() {
        return WRITER;
    }

    public static String toJSON(final Object obj) throws JacksonException {

        String json = null;
        if (obj != null) {
            json = getWriter().writeValueAsString(obj);
        }
        return json;
    }

    public static String toJSONExcludingIgnoredFields(final Object obj) throws JacksonException {
        String json = null;
        if (obj != null) {
            json = WRITER_EXCLUDING_IGNORED_FIELDS.writeValueAsString(obj);
        }
        return json;
    }

    public static <T> T fromJSON(final String json, final Class<T> type)
            throws JacksonException, IOException {

        JsonNode node = getReader().readTree(json);
        T obj = getReader().treeToValue(node, type);
        return obj;

    }

    public static boolean jsonEquals(String json1, String json2)
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
