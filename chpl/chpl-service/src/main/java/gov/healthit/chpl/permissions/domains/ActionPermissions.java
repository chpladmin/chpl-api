package gov.healthit.chpl.permissions.domains;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;

public abstract class ActionPermissions {
    @Autowired
    private ResourcePermissions resourcePermissions;

    public abstract boolean hasAccess();

    public abstract boolean hasAccess(Object obj);

    public boolean isAcbValidForCurrentUser(final Long acbId) {
        List<CertificationBodyDTO> acbs = resourcePermissions.getAllAcbsForCurrentUser();
        for (CertificationBodyDTO dto : acbs) {
            if (dto.getId().equals(acbId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAtlValidForCurrentUser(final Long atlId) {
        List<TestingLabDTO> atls = resourcePermissions.getAllAtlsForCurrentUser();
        for (TestingLabDTO dto : atls) {
            if (dto.getId().equals(atlId)) {
                return true;
            }
        }
        return false;
    }

    public ResourcePermissions getResourcePermissions() {
        return resourcePermissions;
    }
}
