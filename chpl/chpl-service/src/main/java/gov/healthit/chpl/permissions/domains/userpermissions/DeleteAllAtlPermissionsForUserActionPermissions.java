package gov.healthit.chpl.permissions.domains.userpermissions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsDeleteAllAtlPermissionsForUserActionPermissions")
public class DeleteAllAtlPermissionsForUserActionPermissions extends ActionPermissions {

    @Autowired
    private UserTestingLabMapDAO userTestingLabMapDAO;

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
            List<TestingLabDTO> targetAtls = getAtlsUser(targetUserId);
            for (TestingLabDTO targetAtl : targetAtls) {
                if (isAtlValidForCurrentUser(targetAtl.getId())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private List<TestingLabDTO> getAtlsUser(Long userId) {
        List<TestingLabDTO> atls = new ArrayList<TestingLabDTO>();
        List<UserTestingLabMapDTO> userTestingLabMapDTOs = userTestingLabMapDAO.getByUserId(userId);
        for (UserTestingLabMapDTO dto : userTestingLabMapDTOs) {
            atls.add(dto.getTestingLab());
        }
        return atls;
    }
}
