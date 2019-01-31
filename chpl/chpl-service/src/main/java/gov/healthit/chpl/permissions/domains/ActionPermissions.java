package gov.healthit.chpl.permissions.domains;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.UserPermissionsManager;

public abstract class ActionPermissions {
    @Autowired
    private UserPermissionsManager userPermissionsManager;

    public abstract boolean hasAccess();

    public abstract boolean hasAccess(Object obj);

    public boolean isAcbValidForCurrentUser(Long acbId) {
        List<CertificationBodyDTO> acbs = userPermissionsManager.getAllAcbsForCurrentUser();
        for (CertificationBodyDTO dto : acbs) {
            if (dto.getId().equals(acbId)) {
                return true;
            }
        }
        return false;
    }
}
