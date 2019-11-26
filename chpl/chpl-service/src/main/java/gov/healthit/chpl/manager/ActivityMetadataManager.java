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

    List<ActivityMetadata> getTestingLabActivityMetadata(Date startDate, Date endDate)
            throws JsonParseException, IOException;

    List<ActivityMetadata> getTestingLabActivityMetadata(Long atlId, Date startDate, Date endDate)
            throws EntityRetrievalException, JsonParseException, IOException;

    List<ActivityMetadata> getActivityMetadataByConcept(ActivityConcept concept, Date startDate, Date endDate)
            throws JsonParseException, IOException;

    List<ActivityMetadata> getActivityMetadataByObject(Long objectId, ActivityConcept concept, Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityMetadata> getUserMaintenanceActivityMetadata(Date startDate, Date endDate)
            throws JsonParseException, IOException;

    List<ActivityMetadata> getPendingListingActivityMetadata(Date startDate, Date endDate)
            throws IOException;

    List<ActivityMetadata> getPendingSurveillanceActivityMetadata(Date startDate, Date endDate)
            throws IOException;

    List<ActivityMetadata> getAnnouncementActivityMetadata(Date startDate, Date endDate) throws IOException;

    List<ActivityMetadata> getComplaintActivityMetadata(Date startDate, Date endDate) throws IOException;

    List<ActivityMetadata> getQuarterlyReportActivityMetadata(Date startDate, Date endDate) throws IOException;

    List<ActivityMetadata> getQuarterlyReportListingActivityMetadata(Date startDate, Date endDate) throws IOException;

    List<ActivityMetadata> getAnnualReportActivityMetadata(Date startDate, Date endDate) throws IOException;

    List<ActivityMetadata> getChangeRequestActivityMetadata(Date startDate, Date endDate) throws IOException;

    List<ActivityMetadata> getApiKeyManagementMetadata(Date startDate, Date endDate) throws IOException;
}
