package gov.healthit.chpl.permissions.domains;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;

public abstract class ActionPermissions {
    @Autowired
    private CertificationBodyManager acbManager;

    public abstract boolean hasAccess();
    public abstract boolean hasAccess(Object obj);

    public boolean isAcbValidForCurrentUser(Long acbId) {
        List<CertificationBodyDTO> acbs = acbManager.getAllForUser();
        for (CertificationBodyDTO dto : acbs) {
            if (dto.getId().equals(acbId)) {
                return true;
            }
        }
        return false;
    }
}
