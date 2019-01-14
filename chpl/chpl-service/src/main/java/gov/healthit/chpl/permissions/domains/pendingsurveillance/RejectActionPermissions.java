package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class RejectActionPermissions  extends ActionPermissions {
    @Autowired
    private SurveillanceDAO surveillanceDAO;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (!Util.isUserRoleAcbAdmin()) {
            return false;
        } else {
            try {
                //Make sure the user has access to the pendingSurveillance
                Long pendingSurveillanceId = (Long) obj;
                PendingSurveillanceEntity entity = surveillanceDAO.getPendingSurveillanceById(pendingSurveillanceId, true);
                return isAcbValidForCurrentUser(entity.getCertifiedProduct().getCertificationBodyId());
            } catch (Exception e) {
                return false;
            }
        }
    }
}
