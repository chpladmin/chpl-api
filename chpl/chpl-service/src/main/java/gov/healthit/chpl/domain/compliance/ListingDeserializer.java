package gov.healthit.chpl.domain.compliance;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component
public class ListingDeserializer extends JsonDeserializer<Map<Long, String>> {
    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;

    public ListingDeserializer() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public Map<Long, String> deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        //TODO: UNIT TESTS
        Map<Long, String> listings = new LinkedHashMap<Long, String>();
        JsonNode listingDatabaseIdsNode = jsonParser.getCodec().readTree(jsonParser);
        if (listingDatabaseIdsNode != null && listingDatabaseIdsNode.isArray() && listingDatabaseIdsNode.size() > 0) {
            for (JsonNode listingDatabaseIdObj : listingDatabaseIdsNode) {
                Long listingId = listingDatabaseIdObj.asLong();
                if (listingId != null && listingId > 0) {
                    String chplProductNumber = chplProductNumberUtil.generate(listingId);
                    listings.put(listingId, chplProductNumber);
                }
            }
        }
        return listings;
    }
}
