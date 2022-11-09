package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.dao.surveillance.PendingSurveillanceDAO;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class RejectActionPermissions extends ActionPermissions {
    private PendingSurveillanceDAO pendingSurveillanceDAO;
    private UserPermissionDAO userPermissionDAO;

    @Autowired
    public RejectActionPermissions(PendingSurveillanceDAO pendingSurveillanceDAO, UserPermissionDAO userPermissionDAO) {
        this.pendingSurveillanceDAO = pendingSurveillanceDAO;
        this.userPermissionDAO = userPermissionDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        try {
            if (!(obj instanceof Long)) {
                return false;
            } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
                return true;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
                Long pendingSurveillanceId = (Long) obj;
                PendingSurveillanceEntity entity = pendingSurveillanceDAO.getPendingSurveillanceById(pendingSurveillanceId, true);
                // Make sure the user has access to the pendingSurveillance
                return isAcbValidForCurrentUser(entity.getCertifiedProduct().getCertificationBodyId());
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
