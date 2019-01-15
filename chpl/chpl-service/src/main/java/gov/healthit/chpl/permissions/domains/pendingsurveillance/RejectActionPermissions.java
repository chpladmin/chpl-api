package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class RejectActionPermissions  extends ActionPermissions {
    private SurveillanceDAO surveillanceDAO;
    private UserPermissionDAO userPermissionDAO;

    @Autowired
    public RejectActionPermissions(SurveillanceDAO surveillanceDAO, UserPermissionDAO userPermissionDAO) {
        this.surveillanceDAO = surveillanceDAO;
        this.userPermissionDAO = userPermissionDAO;
    }
    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof Long)) {
                return false;
            } else if (Util.isUserRoleAcbAdmin()) {
                Long pendingSurveillanceId = (Long) obj;
                PendingSurveillanceEntity entity = surveillanceDAO.getPendingSurveillanceById(pendingSurveillanceId, true);

                //Make sure the user belongs to the same authority as the pending surveillance
                String authority = userPermissionDAO.findById(entity.getUserPermissionId()).getAuthority();
                if (!authority.equals(Util.ROLE_ACB_AUTHORITY)) {
                    return false;
                } else {
                    //Make sure the user has access to the pendingSurveillance
                    return isAcbValidForCurrentUser(entity.getCertifiedProduct().getCertificationBodyId());
                }
            } else if (Util.isUserRoleOnc() || Util.isUserRoleAdmin()) {
                Long pendingSurveillanceId = (Long) obj;
                PendingSurveillanceEntity entity = surveillanceDAO.getPendingSurveillanceById(pendingSurveillanceId, true);

                //Make sure the user belongs to the same authority as the pending surveillance
                String authority = userPermissionDAO.findById(entity.getUserPermissionId()).getAuthority();
                return authority.equals(Util.ROLE_ONC_AUTHORITY);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
