package gov.healthit.chpl.activity;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnouncementActivityMetadata;
import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.domain.activity.DeveloperActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.domain.activity.ProductActivityMetadata;
import gov.healthit.chpl.domain.activity.TestingLabActivityMetadata;
import gov.healthit.chpl.domain.activity.UserMaintenanceActivityMetadata;
import gov.healthit.chpl.domain.activity.VersionActivityMetadata;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.ActivityDTO;

/**
 * Builds an appropriate metadata object for the type
 * of activity that is provided.
 * @author kekey
 *
 */
public abstract class ActivityMetadataBuilder {

    /**
     * Create an activity metadata object from activity DTO.
     * Fill in the basic fields that all metadata will have (date, id, etc)
     * and then add fields specific to the type of activity.
     * Finally, categorize the activity based on what actually happened.
     * @param dto
     * @return
     */
    public ActivityMetadata build(final ActivityDTO dto) {
        ActivityMetadata metadata = createMetadataObject(dto);
        if (metadata != null) {
            addGenericMetadata(dto, metadata);
            addConceptSpecificMetadata(dto, metadata);
        }
        return metadata;
    }

    protected abstract void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata);

    protected void addGenericMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        metadata.setId(dto.getId());
        metadata.setDate(dto.getActivityDate());
        metadata.setObjectId(dto.getActivityObjectId());
        metadata.setConcept(dto.getConcept());
        metadata.setResponsibleUser(dto.getUser() == null ? null : new User(dto.getUser()));
        metadata.setDescription(dto.getDescription());
    }

    private ActivityMetadata createMetadataObject(final ActivityDTO dto) {
        ActivityMetadata metadata = null;
        switch (dto.getConcept()) {
        case CERTIFIED_PRODUCT:
            metadata = new ListingActivityMetadata();
            break;
        case DEVELOPER:
            metadata = new DeveloperActivityMetadata();
            break;
        case PRODUCT:
            metadata = new ProductActivityMetadata();
            break;
        case VERSION:
            metadata = new VersionActivityMetadata();
            break;
        case CERTIFICATION_BODY:
            metadata = new CertificationBodyActivityMetadata();
            break;
        case TESTING_LAB:
            metadata = new TestingLabActivityMetadata();
            break;
        case USER:
            metadata = new UserMaintenanceActivityMetadata();
            break;
        case ANNOUNCEMENT:
            metadata = new AnnouncementActivityMetadata();
        default:
            break;
        }
        return metadata;
    }
}
