package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ActivityMetadataManager {
    List<ActivityMetadata> getCertificationBodyActivityMetadata(Date startDate, Date endDate)
            throws JsonParseException, IOException;
    List<ActivityMetadata> getCertificationBodyActivityMetadata(Long acbId, Date startDate, Date endDate)
            throws EntityRetrievalException, JsonParseException, IOException;
    List<ActivityMetadata> getActivityMetadataByConcept(
            ActivityConcept concept, Date startDate, Date endDate)
            throws JsonParseException, IOException;
    List<ActivityMetadata> getActivityMetadataByObject(
            Long objectId, ActivityConcept concept,
            Date startDate, Date endDate)
            throws JsonParseException, IOException;
}
