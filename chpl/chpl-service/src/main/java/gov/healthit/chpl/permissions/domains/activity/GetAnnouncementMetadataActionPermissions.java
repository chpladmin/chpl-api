package gov.healthit.chpl.permissions.domains.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetAnnouncementMetadataActionPermissions")
public class GetAnnouncementMetadataActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetAnnouncementMetadataActionPermissions.class);
    private AnnouncementDAO announcementDAO;

    @Autowired
    public GetAnnouncementMetadataActionPermissions(final AnnouncementDAO announcementDAO) {
        this.announcementDAO = announcementDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserAnonymous()) {
            try {
                // Need to look at the Announcement and see if it is public
                ActivityMetadata activityMetadata = (ActivityMetadata) obj;
                AnnouncementDTO announcement = announcementDAO.getById(activityMetadata.getObjectId(), true);
                return announcement.getIsPublic();
            } catch (Exception e) {
                LOGGER.error("There was an error checking permissions.", e);
                return false;
            }
        } else {
            // User is logged in
            return true;
        }
    }

}
