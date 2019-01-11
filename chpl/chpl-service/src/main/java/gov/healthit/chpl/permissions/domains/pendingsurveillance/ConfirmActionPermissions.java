package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class ConfirmActionPermissions extends ActionPermissions{
    @Autowired
    private CertifiedProductDAO cpDAO;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Surveillance)) {
            return false;
        } else if (!Util.isUserRoleAcbAdmin()) {
            return false;
        } else {
            try {
                //Make sure the user has access to the pendingSurveillance
                Surveillance surveillance = (Surveillance) obj;
                CertifiedProductDTO dto = cpDAO.getById(surveillance.getCertifiedProduct().getId());
                return isAcbValidForCurrentUser(dto.getCertificationBodyId());
            } catch (Exception e) {
                return false;
            }
        }
    }

}
