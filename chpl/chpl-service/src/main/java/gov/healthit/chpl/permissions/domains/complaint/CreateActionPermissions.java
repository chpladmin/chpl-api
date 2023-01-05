package gov.healthit.chpl.permissions.domains.complaint;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("complaintCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Complaint)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Complaint complaint = (Complaint) obj;
            return isAcbValidForCurrentUser(complaint.getCertificationBody().getId());
        } else {
            return false;
        }
    }
}
