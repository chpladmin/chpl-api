package gov.healthit.chpl.util;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.search.domain.SearchSetOperator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class StringToSearchSetOperator extends StdDeserializer<SearchSetOperator> {
    private static final long serialVersionUID = -202112287211090833L;

    protected StringToSearchSetOperator() {
        super(Set.class);
    }

    @Override
    public SearchSetOperator deserialize(JsonParser jp, DeserializationContext ctxt) {
        String searchOptionString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(searchOptionString)) {
            return null;
        }

        return SearchSetOperator.valueOf(searchOptionString.toUpperCase());
    }
}
