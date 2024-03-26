package gov.healthit.chpl.activity;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnualReportActivityMetadata;
import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.domain.activity.ChangeRequestActivityMetadata;
import gov.healthit.chpl.domain.activity.ComplaintActivityMetadata;
import gov.healthit.chpl.domain.activity.DeveloperActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.domain.activity.ProductActivityMetadata;
import gov.healthit.chpl.domain.activity.QuarterlyReportActivityMetadata;
import gov.healthit.chpl.domain.activity.TestingLabActivityMetadata;
import gov.healthit.chpl.domain.activity.UserMaintenanceActivityMetadata;
import gov.healthit.chpl.domain.activity.VersionActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;

public class ActivityMetadataBuilder {

    public ActivityMetadata build(ActivityDTO dto) {
        ActivityMetadata metadata = createMetadataObject(dto);
        if (metadata != null) {
            addGenericMetadata(dto, metadata);
            addConceptSpecificMetadata(dto, metadata);
        }
        return metadata;
    }

    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        //can be implemented by subclasses to add other info to the metadata
    }

    protected void addGenericMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        metadata.setId(dto.getId());
        metadata.setDate(dto.getActivityDate());
        metadata.setObjectId(dto.getActivityObjectId());
        metadata.setConcept(dto.getConcept());
        metadata.setResponsibleUser(dto.getUser() == null ? null : dto.getUser().toDomain());
        metadata.setDescription(dto.getDescription());
    }

    private ActivityMetadata createMetadataObject(ActivityDTO dto) {
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
        case COMPLAINT:
            metadata = new ComplaintActivityMetadata();
            break;
        case QUARTERLY_REPORT:
        case QUARTERLY_REPORT_LISTING:
            metadata = new QuarterlyReportActivityMetadata();
            break;
        case ANNUAL_REPORT:
            metadata = new AnnualReportActivityMetadata();
            break;
        case CHANGE_REQUEST:
            metadata = new ChangeRequestActivityMetadata();
            break;
        case ANNOUNCEMENT:
        case CORRECTIVE_ACTION_PLAN:
        case PENDING_SURVEILLANCE:
        case API_KEY:
        case FUNCTIONALITY_TESTED:
        case STANDARD:
        case SVAP:
            metadata = new ActivityMetadata();
            break;
        default:
            break;
        }
        return metadata;
    }
}
