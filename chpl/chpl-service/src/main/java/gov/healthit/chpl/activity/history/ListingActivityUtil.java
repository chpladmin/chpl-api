package gov.healthit.chpl.activity.history;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListingActivityUtil {
    private ObjectMapper jsonMapper;

    public ListingActivityUtil() {
        jsonMapper = new ObjectMapper();
    }

    public CertifiedProductSearchDetails getListing(String listingJson) {
        CertifiedProductSearchDetails listing = null;
        if (!StringUtils.isEmpty(listingJson)) {
            try {
                listing =
                    jsonMapper.readValue(listingJson, CertifiedProductSearchDetails.class);
            } catch (Exception ex) {
                LOGGER.error("Could not parse activity JSON " + listingJson, ex);
            }
        }
        return listing;
    }
}
