package gov.healthit.chpl.util;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.search.domain.DevelopersListingsCriteriaOption;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class StringToDevelopersListingsCriteriaOption extends StdDeserializer<DevelopersListingsCriteriaOption> {
    private static final long serialVersionUID = -681229750063198317L;

    protected StringToDevelopersListingsCriteriaOption() {
        super(Set.class);
    }

    @Override
    public DevelopersListingsCriteriaOption deserialize(JsonParser jp, DeserializationContext ctxt) {
        String listingCriteriaOptionString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(listingCriteriaOptionString)) {
            return null;
        }

        return DevelopersListingsCriteriaOption.valueOf(listingCriteriaOptionString.toUpperCase());
    }
}
