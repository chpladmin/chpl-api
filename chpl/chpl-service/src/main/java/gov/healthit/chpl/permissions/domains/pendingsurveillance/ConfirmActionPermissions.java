package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class ConfirmActionPermissions extends ActionPermissions {
    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public ConfirmActionPermissions(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof Surveillance)) {
                return false;
            } else if (getResourcePermissions().isUserRoleAdmin()) {
                return true;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
                Surveillance surv = (Surveillance) obj;
                CertifiedProductDTO dto = certifiedProductDAO.getById(surv.getCertifiedProduct().getId());
                return isAcbValidForCurrentUser(dto.getCertificationBodyId());
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
