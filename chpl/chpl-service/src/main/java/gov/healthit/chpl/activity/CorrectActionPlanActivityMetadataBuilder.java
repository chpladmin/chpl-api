package gov.healthit.chpl.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;

@Component("correctActionPlanActivityMetadataBuilder")
public class CorrectActionPlanActivityMetadataBuilder extends ActivityMetadataBuilder {

    public CorrectActionPlanActivityMetadataBuilder() {
        super();
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {

    }

}
