package gov.healthit.chpl.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CommaDelimitedStringToSetOfStrings extends StdDeserializer<Set<String>> {
    private static final long serialVersionUID = -4021122872196040833L;

    protected CommaDelimitedStringToSetOfStrings() {
        super(Set.class);
    }

    @Override
    public Set<String> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String commaDelimitedString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(commaDelimitedString)) {
            return null;
        }

        return Arrays.stream(commaDelimitedString.split(","))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
