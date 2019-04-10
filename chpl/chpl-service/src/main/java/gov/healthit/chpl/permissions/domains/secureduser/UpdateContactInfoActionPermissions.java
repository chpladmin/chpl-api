package gov.healthit.chpl.permissions.domains.secureduser;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component("securedUserUpdateContactInfoActionPermissions")
public class UpdateContactInfoActionPermissions extends ActionPermissions {

    @Autowired
    private PermissionEvaluator permissionEvaluator;

    @Override
    public boolean hasAccess() {
        return false;
    }

    //admin can update anyone
    //onc can update anyone
    //acb can update any other user on the same acb
    //atl can update any other user on the same atl
    //any user can update themselves
    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof UserDTO)) {
            return false;
        }
        UserDTO user = (UserDTO) obj;
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                    || permissionEvaluator.hasPermission(AuthUtil.getCurrentUser(), (UserDTO) obj,
                            BasePermission.ADMINISTRATION)) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            //is the user being checked on any of the same ACB(s) that the current user is on?
            List<CertificationBodyDTO> currUserAcbs = getResourcePermissions().getAllAcbsForCurrentUser();
            List<CertificationBodyDTO> otherUserAcbs = getResourcePermissions().getAllAcbsForUser(user.getId());
            for (CertificationBodyDTO currUserAcb : currUserAcbs) {
                for (CertificationBodyDTO otherUserAcb : otherUserAcbs) {
                    if (currUserAcb.getId().equals(otherUserAcb.getId())) {
                        return true;
                    }
                }
            }
        } else if (getResourcePermissions().isUserRoleAtlAdmin()) {
            //is the user being checked on any of the same ATL(s) that the current user is on?
            List<TestingLabDTO> currUserAtls = getResourcePermissions().getAllAtlsForCurrentUser();
            List<TestingLabDTO> otherUserAtls = getResourcePermissions().getAllAtlsForUser(user.getId());
            for (TestingLabDTO currUserAtl : currUserAtls) {
                for (TestingLabDTO otherUserAtl : otherUserAtls) {
                    if (currUserAtl.getId().equals(otherUserAtl.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
