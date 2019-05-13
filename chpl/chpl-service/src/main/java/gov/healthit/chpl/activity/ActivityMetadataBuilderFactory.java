package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.ActivityDTO;

/**
 * Creates an appropriate metadata buidler object for the type of activity.
 * @author kekey
 *
 */
@Component
public class ActivityMetadataBuilderFactory {
    private ListingActivityMetadataBuilder listingBuilder;
    private DeveloperActivityMetadataBuilder developerBuilder;
    private ProductActivityMetadataBuilder productBuilder;
    private VersionActivityMetadataBuilder versionBuilder;
    private CertificationBodyActivityMetadataBuilder acbBuilder;
    private TestingLabActivityMetadataBuilder atlBuilder;
    private UserMaintenanceActivityMetadataBuilder userMaintenanceActivityMetadataBuilder;

    @Autowired
    public ActivityMetadataBuilderFactory(
            @Qualifier("listingActivityMetadataBuilder") final ListingActivityMetadataBuilder listingBuilder,
            @Qualifier("developerActivityMetadataBuilder") final DeveloperActivityMetadataBuilder developerBuilder,
            @Qualifier("productActivityMetadataBuilder") final ProductActivityMetadataBuilder productBuilder,
            @Qualifier("versionActivityMetadataBuilder") final VersionActivityMetadataBuilder versionBuilder,
            @Qualifier("acbActivityMetadataBuilder") final CertificationBodyActivityMetadataBuilder acbBuilder,
            @Qualifier("atlActivityMetadataBuilder") final TestingLabActivityMetadataBuilder atlBuilder,
            @Qualifier("userMaintenanceActivityMetadataBuilder") final UserMaintenanceActivityMetadataBuilder userMaintenanceActivityMetadataBuilder) {
        this.listingBuilder = listingBuilder;
        this.developerBuilder = developerBuilder;
        this.productBuilder = productBuilder;
        this.versionBuilder = versionBuilder;
        this.acbBuilder = acbBuilder;
        this.atlBuilder = atlBuilder;
        this.userMaintenanceActivityMetadataBuilder = userMaintenanceActivityMetadataBuilder;
    }

    /**
     * Factory method to get a metadata builder of the appropriate class
     * based on what type of activity object is passed in.
     * @param dto the activity object
     * @return the appropriate builder
     */
    public ActivityMetadataBuilder getBuilder(final ActivityDTO dto) {
        ActivityMetadataBuilder builder = null;
        switch (dto.getConcept()) {
        case CERTIFIED_PRODUCT:
            builder = listingBuilder;
            break;
        case DEVELOPER:
            builder = developerBuilder;
            break;
        case PRODUCT:
            builder = productBuilder;
            break;
        case VERSION:
            builder = versionBuilder;
            break;
        case CERTIFICATION_BODY:
            builder = acbBuilder;
            break;
        case TESTING_LAB:
            builder = atlBuilder;
            break;
        case USER:
            builder = userMaintenanceActivityMetadataBuilder;
        default:
            break;
        }
        return builder;
    }
}
