package gov.healthit.chpl.domain.compliance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import gov.healthit.chpl.util.ChplProductNumberUtil;

public class ListingDeserializer extends JsonDeserializer<List<DeveloperAssociatedListing>> {
    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;

    public ListingDeserializer() {
        System.out.println("Deserializer constructor called.");
        System.out.println("chplProductNumberUtil: " + chplProductNumberUtil);
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        System.out.println("Deserializer constructor complete.");
        System.out.println("chplProductNumberUtil: " + chplProductNumberUtil);
    }

    @Override
    public List<DeveloperAssociatedListing> deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        System.out.println("chplProductNumberUtil: " + chplProductNumberUtil);
        List<DeveloperAssociatedListing> listings = new ArrayList<DeveloperAssociatedListing>();
        JsonNode listingDatabaseIdsNode = jsonParser.getCodec().readTree(jsonParser);
        if (listingDatabaseIdsNode != null && listingDatabaseIdsNode.isArray() && listingDatabaseIdsNode.size() > 0) {
            for (JsonNode listingDatabaseIdObj : listingDatabaseIdsNode) {
                Long listingId = listingDatabaseIdObj.asLong();
                if (listingId != null && listingId > 0) {
                    String chplProductNumber = chplProductNumberUtil.generate(listingId);
                    listings.add(new DeveloperAssociatedListing(listingId, chplProductNumber));
                }
            }
        }
        return listings;
    }
}
