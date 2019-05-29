package gov.healthit.chpl.permissions.domains.complaint;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("complaintGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ComplaintDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ComplaintDTO complaint = (ComplaintDTO) obj;
            return isAcbValidForCurrentUser(complaint.getCertificationBody().getId());
        } else {
            return false;
        }

    }

}
