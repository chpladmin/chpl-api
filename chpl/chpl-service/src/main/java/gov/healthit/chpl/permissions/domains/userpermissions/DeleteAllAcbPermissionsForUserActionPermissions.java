package gov.healthit.chpl.permissions.domains.userpermissions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsDeleteAllAcbPermissionsForUserActionPermissions")
public class DeleteAllAcbPermissionsForUserActionPermissions extends ActionPermissions {

    @Autowired
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAtlAdmin()) {
            Long targetUserId = (Long) obj;
            List<CertificationBodyDTO> targetAcbs = getAcbsUser(targetUserId);
            for (CertificationBodyDTO targetAcb : targetAcbs) {
                if (isAcbValidForCurrentUser(targetAcb.getId())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private List<CertificationBodyDTO> getAcbsUser(Long userId) {
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        List<UserCertificationBodyMapDTO> userCertificationBodyMapDTOs = userCertificationBodyMapDAO
                .getByUserId(userId);
        for (UserCertificationBodyMapDTO dto : userCertificationBodyMapDTOs) {
            acbs.add(dto.getCertificationBody());
        }
        return acbs;
    }

}
