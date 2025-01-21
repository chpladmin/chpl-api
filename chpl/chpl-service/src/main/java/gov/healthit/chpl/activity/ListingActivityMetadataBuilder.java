package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("listingActivityMetadataBuilder")
public class ListingActivityMetadataBuilder extends ActivityMetadataBuilder {

    private ListingSearchService listingSearchService;

    @Autowired
    public ListingActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil,
            ListingSearchService listingSearchService) {
        super(chplUserToCognitoUserUtil);
        this.listingSearchService = listingSearchService;
    }

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof ListingActivityMetadata)) {
            return;
        }
        ListingActivityMetadata listingMetadata = (ListingActivityMetadata) metadata;
        listingMetadata.getCategories().add(ActivityCategory.LISTING);
        if (metadata.getObject() != null && metadata.getObject().getId() != null) {
            try {
                ListingSearchResult listingSearchResult = listingSearchService.findListing(metadata.getObject().getId());
                if (listingSearchResult != null) {
                    metadata.getObject().setName(listingSearchResult.getChplProductNumber());
                }
            } catch (Exception ex) {
                LOGGER.error("Could not find listing " + metadata.getObject().getId() + " for activity metadata.", ex);
            }
        }
    }
}
