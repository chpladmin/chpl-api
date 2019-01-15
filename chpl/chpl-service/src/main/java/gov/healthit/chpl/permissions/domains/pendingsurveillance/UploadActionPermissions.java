package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component
public class UploadActionPermissions extends ActionPermissions{
    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public UploadActionPermissions(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }
    @Override
    public boolean hasAccess() {
        return Util.isUserRoleAcbAdmin() || Util.isUserRoleAdmin() || Util.isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof Surveillance)) {
                return false;
            } else if (Util.isUserRoleAcbAdmin()) {
                Surveillance surv = (Surveillance) obj;
                //Make sure the pending surveillance belongs to the correct authority
                if (!surv.getAuthority().equals(Util.ROLE_ACB_AUTHORITY)) {
                    return false;
                } else {
                    //Make sure the user has access to the pending surv acb
                    CertifiedProductDTO dto = certifiedProductDAO.getById(surv.getCertifiedProduct().getId());
                    return isAcbValidForCurrentUser(dto.getCertificationBodyId());
                }
            } else if (Util.isUserRoleOnc() || Util.isUserRoleAdmin()) {
                Surveillance surv = (Surveillance) obj;
                //Make sure the pending surveillance belongs to the correct authority
                return surv.getAuthority().equals(Util.ROLE_ONC_AUTHORITY);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
