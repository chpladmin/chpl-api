package gov.healthit.chpl.permissions.domains.complaint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("complaintDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {

    private ComplaintDAO complaintDAO;

    @Autowired
    public DeleteActionPermissions(final ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    @Transactional
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long complaintId = (Long) obj;
            try {
                ComplaintDTO complaint = complaintDAO.getComplaint(complaintId);
                return isAcbValidForCurrentUser(complaint.getCertificationBody().getId());
            } catch (EntityRetrievalException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
