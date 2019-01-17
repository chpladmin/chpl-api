package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class RejectActionPermissions  extends ActionPermissions {
    private SurveillanceDAO surveillanceDAO;
    private UserPermissionDAO userPermissionDAO;

    @Autowired
    public RejectActionPermissions(final SurveillanceDAO surveillanceDAO,
            final UserPermissionDAO userPermissionDAO) {
        this.surveillanceDAO = surveillanceDAO;
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
            } else if (Util.isUserRoleAcbAdmin()) {
                Long pendingSurveillanceId = (Long) obj;
                PendingSurveillanceEntity entity = surveillanceDAO.getPendingSurveillanceById(pendingSurveillanceId);

                //Make sure the user belongs to the same authority as the pending surveillance
                String authority = userPermissionDAO.findById(entity.getUserPermissionId()).getAuthority();
                if (!authority.equals(Authority.ROLE_ACB)) {
                    return false;
                } else {
                    //Make sure the user has access to the pendingSurveillance
                    return isAcbValidForCurrentUser(entity.getCertifiedProduct().getCertificationBodyId());
                }
            } else if (Util.isUserRoleOnc() || Util.isUserRoleAdmin()) {
                Long pendingSurveillanceId = (Long) obj;
                PendingSurveillanceEntity entity = surveillanceDAO.getPendingSurveillanceById(pendingSurveillanceId);

                //Make sure the user belongs to the same authority as the pending surveillance
                String authority = userPermissionDAO.findById(entity.getUserPermissionId()).getAuthority();
                return authority.equals(Authority.ROLE_ONC);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
