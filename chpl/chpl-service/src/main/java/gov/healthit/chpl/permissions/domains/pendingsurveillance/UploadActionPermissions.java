package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class UploadActionPermissions extends ActionPermissions{
    @Autowired
    private CertifiedProductDAO cpDAO;

    @Override
    public boolean hasAccess() {
        return Util.isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Surveillance)) {
            return false;
        } else if (Util.isUserRoleAcbAdmin()) {
            Surveillance surveillance = (Surveillance) obj;
            CertifiedProductDTO dto = null;
            try {
                dto = cpDAO.getById(surveillance.getCertifiedProduct().getId());
            } catch (EntityRetrievalException e) {
                return false;
            }
            return isAcbValidForCurrentUser(dto.getCertificationBodyId());
        } else {
            return false;
        }
    }
}
