package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.DeveloperActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;

/**
 * Creates an appropriate metadata buidler object for the type of activity
 * @author kekey
 *
 */
@Component
public class ActivityMetadataBuilderFactory {
    @Autowired
    @Qualifier("listingActivityMetadataBuilder")
    private ListingActivityMetadataBuilder listingBuilder;

    @Autowired
    @Qualifier("developerActivityMetadataBuilder")
    private DeveloperActivityMetadataBuilder developerBuilder;

    public ActivityMetadataBuilder getBuilder(final ActivityDTO dto) {
        ActivityMetadataBuilder builder = null;
        switch (dto.getConcept()) {
        case CERTIFIED_PRODUCT:
            builder = listingBuilder;
            break;
        case DEVELOPER:
            builder = developerBuilder;
            break;
        default:
            break;
        }
        return builder;
    }
}
