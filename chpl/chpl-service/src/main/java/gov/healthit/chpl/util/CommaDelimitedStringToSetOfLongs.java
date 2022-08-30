package gov.healthit.chpl.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CommaDelimitedStringToSetOfLongs extends StdDeserializer<Set<Long>> {
    private static final long serialVersionUID = -4021122872196040833L;

    protected CommaDelimitedStringToSetOfLongs() {
        super(Set.class);
    }

    @Override
    public Set<Long> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String commaDelimitedString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(commaDelimitedString)) {
            return null;
        }

        String[] stringArr = commaDelimitedString.split(",");
        if (stringArr == null || stringArr.length == 0) {
            return new HashSet<Long>();
        }

        Set<Long> setOfLongs = new HashSet<Long>();
        Arrays.stream(stringArr)
            .filter(str -> isValidLong(str))
            .forEach(str -> setOfLongs.add(toLong(str)));
        return setOfLongs;
    }

    private boolean isValidLong(String str) {
        try {
            Long.parseLong(str);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private Long toLong(String str) {
        return Long.parseLong(str);
    }
}
