package gov.healthit.chpl.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.flipkart.zjsonpatch.JsonDiff;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class JSONUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final ObjectReader READER = MAPPER.reader();
    private static final ObjectWriter WRITER = MAPPER.writer();

    private static final ObjectMapper MAPPER_WITHOUT_DEPRECATED_FIELDS = new ObjectMapper().findAndRegisterModules()
            .setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                private static final long serialVersionUID = -1856550954546461022L;

                @Override
                public boolean hasIgnoreMarker(final AnnotatedMember m) {
                    return super.hasIgnoreMarker(m) || m.hasAnnotation(Deprecated.class);
                }
            });
    private static final ObjectWriter WRITER_WITHOUT_DEPRECATED_FIELDS = MAPPER_WITHOUT_DEPRECATED_FIELDS.writer();

    private JSONUtils() {
    }

    public static ObjectReader getReader() {
        return READER;
    }

    public static ObjectWriter getWriter() {
        return WRITER;
    }

    public static String toJSON(final Object obj) throws JsonProcessingException {

        String json = null;
        if (obj != null) {
            json = getWriter().writeValueAsString(obj);
        }
        return json;
    }

    public static String toJSONIgnoringDeprecatedFields(final Object obj) throws JsonProcessingException {
        String json = null;
        if (obj != null) {
            json = WRITER_WITHOUT_DEPRECATED_FIELDS.writeValueAsString(obj);
        }
        return json;
    }

    public static <T> T fromJSON(final String json, final Class<T> type)
            throws JsonProcessingException, IOException {

        JsonNode node = getReader().readTree(json);
        T obj = getReader().treeToValue(node, type);
        return obj;

    }

    public static boolean jsonEquals(String json1, String json2)
            throws JsonProcessingException, IOException {
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

            JsonNode patch = JsonDiff.asJson(node1, node2);
            if (patch != null && !patch.isEmpty()) {
                LOGGER.info("DIFFERENCES FOUND");
                LOGGER.info(patch.toString());
            }

        } catch (final NullPointerException e) {
            equals = false;
        }
        return equals;
    }

}
