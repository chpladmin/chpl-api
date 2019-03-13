package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.domain.activity.ActivityMetadata;

public interface ActivityMetadataManager {
    List<ActivityMetadata> getListingActivityMetadata(final Date startDate, final Date endDate)
            throws JsonParseException, IOException;
    List<ActivityMetadata> getListingActivityMetadata(final Long listingId, final Date startDate, final Date endDate)
            throws JsonParseException, IOException;
}
