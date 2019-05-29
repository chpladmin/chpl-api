package gov.healthit.chpl.permissions.domains.activity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetUserMaintenanceMetadataActionPermissions")
public class GetUserMaintenanceMetadataActionPermissions extends ActionPermissions {
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserTestingLabMapDAO userTestingLabMapDAO;
    private UserDAO userDAO;

    @Autowired
    public GetUserMaintenanceMetadataActionPermissions(final UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            final UserTestingLabMapDAO userTestingLabMapDAO, final UserDAO userDAO) {
        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.userTestingLabMapDAO = userTestingLabMapDAO;
        this.userDAO = userDAO;
    }

    @Override
    public boolean hasAccess() {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleAcbAdmin()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ActivityMetadata activity = (ActivityMetadata) obj;
            return checkIfCurrentUserHasAcbAccessToUser(activity.getObjectId());
        } else if (getResourcePermissions().isUserRoleAtlAdmin()) {
            ActivityMetadata activity = (ActivityMetadata) obj;
            return checkIfCurrentUserHasAtlAccessToUser(activity.getObjectId());
        } else if (getResourcePermissions().isUserRoleCmsStaff()) {
            ActivityMetadata activity = (ActivityMetadata) obj;
            return checkIfCurrentUserHasCmsAccessToUser(activity.getObjectId());
        } else {
            return false;
        }
    }

    private Boolean checkIfCurrentUserHasAcbAccessToUser(Long userId) {
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userId);
        for (UserCertificationBodyMapDTO dto : dtos) {
            if (!isAcbValidForCurrentUser(dto.getCertificationBody().getId())) {
                return false;
            }
        }
        return true;
    }

    private Boolean checkIfCurrentUserHasAtlAccessToUser(Long userId) {
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(userId);
        for (UserTestingLabMapDTO dto : dtos) {
            if (!isAtlValidForCurrentUser(dto.getTestingLab().getId())) {
                return false;
            }
        }
        return true;
    }

    private Boolean checkIfCurrentUserHasCmsAccessToUser(Long userId) {
        UserDTO user;
        try {
            user = userDAO.getById(userId);
        } catch (UserRetrievalException e) {
            return false;
        }
        return user.getPermission().getAuthority().equals(Authority.ROLE_CMS_STAFF);
    }
}
