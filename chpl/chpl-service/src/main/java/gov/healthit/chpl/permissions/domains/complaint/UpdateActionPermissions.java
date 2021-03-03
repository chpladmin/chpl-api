package gov.healthit.chpl.permissions.domains.complaint;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("complaintUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Complaint)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Complaint complaint = (Complaint) obj;
            return isAcbValidForCurrentUser(complaint.getCertificationBody().getId());
        } else {
            return false;
        }
    }
}
