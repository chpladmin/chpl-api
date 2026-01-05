package gov.healthit.chpl.domain.compliance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.ChplProductNumberUtil;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

@Component
public class ListingDeserializer extends StdDeserializer<List<DeveloperAssociatedListing>> {
    private static final String ID_FIELD = "id";
    private static final String CHPL_PRODUCT_NUMBER_FIELD = "chplProductNumber";

    protected ListingDeserializer() {
        super(DeveloperAssociatedListing.class);
    }

    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;

    /*********************************************
     * This deserializer handles 2 different situations:
     * 1. When data from Jira is deserialized into a DeveloperAssociatedListing
     *   a. In this case, the data is simply a listing id and the CHPL Prd Nbr
     *      needs to be generated
     * 2. When the data already represents a valid DeveloperAssociatedListing, ie.
     *    the data is coming from the SharedStore
     *   a. In this case, the data needs to be read from the JsonNodes and put in
     *      in a DeveloperAssociatedListing object
    *********************************************/
    @Override
    public List<DeveloperAssociatedListing> deserialize(JsonParser jsonParser, DeserializationContext context) {
        List<DeveloperAssociatedListing> listings = new ArrayList<DeveloperAssociatedListing>();
        JsonNode listingDatabaseIdsNode = jsonParser.objectReadContext().readTree(jsonParser);
        if (listingDatabaseIdsNode != null && listingDatabaseIdsNode.isArray() && listingDatabaseIdsNode.size() > 0) {
            for (JsonNode listingDatabaseIdObj : listingDatabaseIdsNode) {
                Long listingId = null;
                String chplProductNumber = null;
                if (representsDeveloperAssociatedListingObject(listingDatabaseIdsNode)) {
                    // From an existing DeveloperAssociatedListing form {"id":10764,"chplProductNumber":"15.04.04.2883.eCli.11.01.1.211228"}
                    listingId = listingDatabaseIdObj.findValue(ID_FIELD).asLong();
                    chplProductNumber = listingDatabaseIdObj.findValue(CHPL_PRODUCT_NUMBER_FIELD).asString();
                } else {
                    //From a listing id - form "10989"
                    listingId = listingDatabaseIdObj.asLong();
                    if (listingId != null && listingId > 0) {
                        chplProductNumber = chplProductNumberUtil.generate(listingId);
                    }
                }
                listings.add(DeveloperAssociatedListing.builder()
                        .id(listingId)
                        .chplProductNumber(chplProductNumber)
                        .build());
            }
        }
        return listings;
    }

    private Boolean representsDeveloperAssociatedListingObject(JsonNode node) {
        try {
            return node.findValue(ID_FIELD) != null
                    && node.findValue(CHPL_PRODUCT_NUMBER_FIELD) != null;
        } catch (Exception e) {
            return false;
        }
     }
}
