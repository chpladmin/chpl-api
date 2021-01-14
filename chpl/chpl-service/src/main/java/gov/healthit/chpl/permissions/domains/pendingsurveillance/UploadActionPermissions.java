package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class UploadActionPermissions extends ActionPermissions {
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public UploadActionPermissions(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof Surveillance)) {
                return false;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
                Surveillance surv = (Surveillance) obj;
                // Make sure the user has access to the pending surv acb
                CertifiedProductDTO dto = certifiedProductDAO.getById(surv.getCertifiedProduct().getId());
                return isAcbValidForCurrentUser(dto.getCertificationBodyId());
            } else {
                return getResourcePermissions().isUserRoleOnc()
                        || getResourcePermissions().isUserRoleOncStaff()
                        || getResourcePermissions().isUserRoleAdmin();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
