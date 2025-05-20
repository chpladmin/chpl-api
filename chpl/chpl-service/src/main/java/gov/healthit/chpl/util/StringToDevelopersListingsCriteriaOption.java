package gov.healthit.chpl.util;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import gov.healthit.chpl.search.domain.DevelopersListingsCriteriaOption;

public class StringToDevelopersListingsCriteriaOption extends StdDeserializer<DevelopersListingsCriteriaOption> {
    private static final long serialVersionUID = -681229750063198317L;

    protected StringToDevelopersListingsCriteriaOption() {
        super(Set.class);
    }

    @Override
    public DevelopersListingsCriteriaOption deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String listingCriteriaOptionString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(listingCriteriaOptionString)) {
            return null;
        }

        return DevelopersListingsCriteriaOption.valueOf(listingCriteriaOptionString.toUpperCase());
    }
}
