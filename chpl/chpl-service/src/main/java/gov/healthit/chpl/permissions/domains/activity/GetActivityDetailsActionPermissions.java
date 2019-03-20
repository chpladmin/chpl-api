package gov.healthit.chpl.permissions.domains.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetActivityDetailsActionPermissions")
public class GetActivityDetailsActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetActivityDetailsActionPermissions.class);

    @Autowired
    private PermissionEvaluator permissionEvaluator;

    @Autowired
    private UserManager userManager;

    private ObjectMapper jsonMapper;

    public GetActivityDetailsActionPermissions() {
        jsonMapper = new ObjectMapper();
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof ActivityDetails)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            ActivityDetails details = (ActivityDetails) obj;
            //There are different security rules depending on what type
            //of activity this is.
            switch (details.getConcept()) {
            case ANNOUNCEMENT:
                if (details.getNewData() != null) {
                    return hasAccessToAnnouncement(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToAnnouncement(details.getOriginalData());
                }
                break;
            case ATL:
                if (details.getNewData() != null) {
                    return hasAccessToTestingLab(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToTestingLab(details.getOriginalData());
                }
                break;
            case CERTIFICATION_BODY:
                if (details.getNewData() != null) {
                    return hasAccessToCertificationBody(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToCertificationBody(details.getOriginalData());
                }
                break;
            case API_KEY:
                //Admin and Onc can see this activity.
                //Others should get access denied.
                return (getResourcePermissions().isUserRoleAdmin()
                        || getResourcePermissions().isUserRoleOnc());
            case PENDING_CERTIFIED_PRODUCT:
                if (details.getNewData() != null) {
                    return hasAccessToPendingListing(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToPendingListing(details.getOriginalData());
                }
                break;
            case USER:
                if (details.getNewData() != null) {
                    return hasAccessToUser(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToUser(details.getOriginalData());
                }
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

        if (!hasAccess) {
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

    /**
     * Admin and onc can see all acb activity
     * including for retired acbs.
     * Acb user can see activity for their own acb.
     * Others should get access denied.
     * @param acbJson
     * @return
     */
    private boolean hasAccessToCertificationBody(final JsonNode acbJson) {
        boolean hasAccess = getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();

        if (!hasAccess) {
            CertificationBodyDTO acb = null;
            try {
                acb =
                    jsonMapper.convertValue(acbJson, CertificationBodyDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity as CertificationBodyDTO. "
                        + "JSON was: " + acbJson, ex);
            }

            if (acb != null && acb.getId() != null) {
                hasAccess = isAcbValidForCurrentUser(acb.getId());
            }
        }
        return hasAccess;
    }

    /**
     * Admin and onc can see all pending listing activity.
     * Acb user can see activity for listing uploaded to their Acb.
     * Others should get access denied.
     * @param pendingListingJson
     * @return
     */
    private boolean hasAccessToPendingListing(final JsonNode pendingListingJson) {
        boolean hasAccess = getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();

        if (!hasAccess) {
            PendingCertifiedProductDTO pcp = null;
            try {
                pcp =
                    jsonMapper.convertValue(pendingListingJson, PendingCertifiedProductDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity as PendingCertifiedProductDTO. "
                        + "JSON was: " + pendingListingJson, ex);
            }

            if (pcp != null && pcp.getCertificationBodyId() != null) {
                hasAccess = isAcbValidForCurrentUser(pcp.getCertificationBodyId());
            }
        }
        return hasAccess;
    }

    /**
     * Admin and Onc can see activity for any user.
     * Acb, Atl, and Cms staff users can see activity for any user
     * they have access to.
     * Non-logged in users get access denied.
     * @param userJson
     * @return
     */
    private boolean hasAccessToUser(final JsonNode userJson) {
        boolean hasAccess = getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();

        if (!hasAccess) {
            UserDTO user = null;
            try {
                user =
                    jsonMapper.convertValue(userJson, UserDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity as UserDTO. "
                        + "JSON was: " + userJson, ex);
            }

            if (user != null) {
                List<UserDTO> accessibleUsers = new ArrayList<UserDTO>();
                if (getResourcePermissions().isUserRoleAcbAdmin()) {
                    //find all users on the acbs that this user has access to
                    //and see if the user in the activity is in that list
                    List<CertificationBodyDTO> accessibleAcbs = getResourcePermissions().getAllAcbsForCurrentUser();
                    for (CertificationBodyDTO acb : accessibleAcbs) {
                        accessibleUsers.addAll(getResourcePermissions().getAllUsersOnAcb(acb));
                    }
                }
                if (getResourcePermissions().isUserRoleAtlAdmin()) {
                    //find all users on the atls that this user has access to
                    //and see if the user in the activity is in that list
                    List<TestingLabDTO> accessibleAtls = getResourcePermissions().getAllAtlsForCurrentUser();
                    for (TestingLabDTO atl : accessibleAtls) {
                        accessibleUsers.addAll(getResourcePermissions().getAllUsersOnAtl(atl));
                    }
                }
                if (getResourcePermissions().isUserRoleCmsStaff()) {
                    List<UserDTO> cmsUsers = userManager.getUsersWithPermission(Authority.ROLE_CMS_STAFF);
                    accessibleUsers.addAll(cmsUsers);
                }

                for (UserDTO accessibleUser : accessibleUsers) {
                    if (accessibleUser.getId().longValue() == user.getId().longValue()) {
                        hasAccess = true;
                    }
                }
            }
        }
        return hasAccess;
    }
}
