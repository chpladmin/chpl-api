package gov.healthit.chpl.sharedstore.listing;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.ics.IcsManager;
import gov.healthit.chpl.listing.ics.ListingIcsNode;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingIcsSharedStoreHandler {

    private SharedListingStoreProvider sharedListingStoreProvider;
    private IcsManager icsManager;

    @Autowired
    public ListingIcsSharedStoreHandler(SharedListingStoreProvider sharedListingStoreProvider,
            IcsManager icsManager) {
        this.sharedListingStoreProvider = sharedListingStoreProvider;
        this.icsManager = icsManager;
    }

    public void handle(Long listingId) {
        List<Long> relativeIds = getCertifiedProductRelativeIds(listingId);
        if (!CollectionUtils.isEmpty(relativeIds)) {
            sharedListingStoreProvider.remove(relativeIds);
        }
    }

    private List<Long> getCertifiedProductRelativeIds(Long listingId) {
        List<ListingIcsNode> icsRelatives = null;
        try {
            icsRelatives = icsManager.getIcsFamilyTree(listingId);
        } catch (EntityRetrievalException ex) {
            LOGGER.warn("Not deleting any ICS relatives. Listing with ID " + listingId + " was not found.");
        }
        if (!CollectionUtils.isEmpty(icsRelatives)) {
            return icsRelatives.stream()
                    .filter(relative -> !relative.getId().equals(listingId))
                    .map(relative -> relative.getId())
                    .toList();
        }
        return null;
    }
}
