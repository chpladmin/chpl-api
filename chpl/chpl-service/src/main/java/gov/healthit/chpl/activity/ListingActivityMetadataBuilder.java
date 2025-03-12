package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("listingActivityMetadataBuilder")
public class ListingActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Autowired
    public ListingActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil) {
        super(chplUserToCognitoUserUtil);
    }

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof ListingActivityMetadata)) {
            return;
        }
        ListingActivityMetadata listingMetadata = (ListingActivityMetadata) metadata;
        listingMetadata.getCategories().add(ActivityCategory.LISTING);
    }
}
