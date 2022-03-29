package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("activityGetAnnouncementMetadataActionPermissions")
public class GetAnnouncementMetadataActionPermissions extends ActionPermissions {
    private AnnouncementDAO announcementDAO;

    @Autowired
    public GetAnnouncementMetadataActionPermissions(AnnouncementDAO announcementDAO) {
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
                Announcement announcement = announcementDAO.getById(activityMetadata.getObjectId(), true);
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
