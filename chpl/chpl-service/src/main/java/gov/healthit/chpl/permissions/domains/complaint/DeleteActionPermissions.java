package gov.healthit.chpl.permissions.domains.complaint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("complaintDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {

    private ComplaintDAO complaintDao;

    @Autowired
    public DeleteActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao,
            ComplaintDAO complaintDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        this.complaintDao = complaintDao;
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
        } else if (getResourcePermissions().isUserRoleAdmin()) {
           return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long complaintId = (Long) obj;
            try {
                Complaint complaint = complaintDao.getComplaint(complaintId);
                return isAcbValidForCurrentUser(complaint.getCertificationBody().getId());
            } catch (EntityRetrievalException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
