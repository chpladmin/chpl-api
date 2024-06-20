package gov.healthit.chpl.util;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import gov.healthit.chpl.search.domain.SearchSetOperator;

public class StringToSearchSetOperator extends StdDeserializer<SearchSetOperator> {
    private static final long serialVersionUID = -202112287211090833L;

    protected StringToSearchSetOperator() {
        super(Set.class);
    }

    @Override
    public SearchSetOperator deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String searchOptionString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(searchOptionString)) {
            return null;
        }

        return SearchSetOperator.valueOf(searchOptionString.toUpperCase());
    }
}
