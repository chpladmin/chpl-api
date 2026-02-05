package gov.healthit.chpl.developer.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class CommaDelimitedStringToSetOfActiveListingSearchOptions extends StdDeserializer<Set<ActiveListingSearchOptions>> {
    private static final long serialVersionUID = -4061122872196040833L;

    protected CommaDelimitedStringToSetOfActiveListingSearchOptions() {
        super(Set.class);
    }

    @Override
    public Set<ActiveListingSearchOptions> deserialize(JsonParser jp, DeserializationContext ctxt) {
        String commaDelimitedString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(commaDelimitedString)) {
            return null;
        }

        return Arrays.stream(commaDelimitedString.split(","))
                .map(str -> ActiveListingSearchOptions.valueOf(str.toUpperCase()))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
