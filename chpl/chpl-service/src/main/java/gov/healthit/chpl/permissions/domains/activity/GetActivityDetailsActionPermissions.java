package gov.healthit.chpl.permissions.domains.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetActivityDetailsActionPermissions")
public class GetActivityDetailsActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetActivityDetailsActionPermissions.class);

    @Autowired
    private PermissionEvaluator permissionEvaluator;
    private ObjectMapper jsonMapper;

    public GetActivityDetailsActionPermissions() {
        jsonMapper = new ObjectMapper();
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ActivityDetails)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            ActivityDetails details = (ActivityDetails) obj;
            //Check security based on the activity concept
            switch (details.getConcept()) {
            case ANNOUNCEMENT:
                if (details.getNewData() != null) {
                    return hasAccessToAnnouncement(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToAnnouncement(details.getOriginalData());
                }
                break;
            case ATL:
                return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                        || isAtlValidForCurrentUser(atlId);
                break;
            case CERTIFICATION_BODY:
                //Admin and onc can see all acb activity
                //including for retired acbs.
                //Acb user can see activity for their own acb.
                //Others should get access denied.
                return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                        || isAcbValidForCurrentUser(acbId);
                break;
            case API_KEY:
                //Admin and Onc can see this activity.
                //Others should get access denied.
                return (getResourcePermissions().isUserRoleAdmin()
                        || getResourcePermissions().isUserRoleOnc());
                break;
            case PENDING_CERTIFIED_PRODUCT:
                //Admin and Onc can see this activity.
                //Acb user can see activity for listing uploaded to their Acb.
                //Others should get access denied.
                return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                        || isAcbValidForCurrentUser(acbId);
                break;
            case USER:
                //Admin and Onc can see activity for any user.
                //Acb, Atl, and Cms staff users can see activity for any user
                //they have access to.
                //Non-logged in users get access denied.
                return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                        || permissionEvaluator.hasPermission(Util.getCurrentUser(), userDto,
                                BasePermission.ADMINISTRATION);
                break;
            default:
                //all other types of activity
                //are accessible to any logged-in or anonymous user
                return true;
            }
        }
        return false;
    }

    /**
     * Non-logged in user can only see activity for public announcements.
     * Other users can see all activity.
     * @param announcementJson
     * @return
     */
    private boolean hasAccessToAnnouncement(final JsonNode announcementJson) {
        boolean hasAccess = false;
        AnnouncementDTO announcement = null;
        try {
            announcement =
                jsonMapper.convertValue(announcementJson, AnnouncementDTO.class);
        } catch (final Exception ex) {
            LOGGER.error("Could not parse announcement activity as AnnouncementDTO."
                    + "JSON was: " + announcementJson, ex);
        }

        if (announcement != null && announcement.getIsPublic()) {
            hasAccess = true;
        } else if (announcement != null && !announcement.getIsPublic()
                && !getResourcePermissions().isUserAnonymous()) {
            hasAccess = true;
        }
        return hasAccess;
    }

    /**
     * Admin and onc can see all atl activity
     * including for retired atls.
     * Atl user can see activity for their own atl.
     * Others should get access denied.
     * @param atlJson
     * @return
     */
    private boolean hasAccessToTestingLab(final JsonNode atlJson) {
        boolean hasAccess = getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();

        if (hasAccess = false) {
            TestingLabDTO atl = null;
            try {
                atl =
                    jsonMapper.convertValue(atlJson, TestingLabDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity as TestingLabDTO. "
                        + "JSON was: " + atlJson, ex);
            }

            if (atl != null && atl.getId() != null) {
                hasAccess = isAtlValidForCurrentUser(atl.getId());
            }
        }
        return hasAccess;
    }
}
