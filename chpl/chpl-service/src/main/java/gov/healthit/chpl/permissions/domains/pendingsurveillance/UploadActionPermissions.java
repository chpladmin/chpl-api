package gov.healthit.chpl.permissions.domains.pendingsurveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductDAO;
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
        return false;
    }
}
