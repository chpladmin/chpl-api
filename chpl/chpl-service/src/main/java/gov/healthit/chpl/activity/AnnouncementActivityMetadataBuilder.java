package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.UserMaintenanceActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;

@Component("announcementActivityMetadataBuilder")
public class AnnouncementActivityMetadataBuilder extends ActivityMetadataBuilder {

    private static final Logger LOGGER = LogManager.getLogger(VersionActivityMetadataBuilder.class);

    public AnnouncementActivityMetadataBuilder() {
        super();
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        if (!(metadata instanceof UserMaintenanceActivityMetadata)) {
            return;
        }
    }

}
