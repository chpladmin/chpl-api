package gov.healthit.chpl.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;

@Component("pendingSurveillanceActivityMetadataBuilder")
public class PendingSurveillanceActivityMetadataBuilder extends ActivityMetadataBuilder {

    public PendingSurveillanceActivityMetadataBuilder() {
        super();
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {

    }
}
