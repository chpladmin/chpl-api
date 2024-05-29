package gov.healthit.chpl.developer.search;

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

public class CommaDelimitedStringToSetOfAttestationsSearchOptions extends StdDeserializer<Set<AttestationsSearchOptions>> {
    private static final long serialVersionUID = -1021122872196040833L;

    protected CommaDelimitedStringToSetOfAttestationsSearchOptions() {
        super(Set.class);
    }

    @Override
    public Set<AttestationsSearchOptions> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String commaDelimitedString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(commaDelimitedString)) {
            return null;
        }

        return Arrays.stream(commaDelimitedString.split(","))
                .map(str -> AttestationsSearchOptions.valueOf(str.toUpperCase()))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
