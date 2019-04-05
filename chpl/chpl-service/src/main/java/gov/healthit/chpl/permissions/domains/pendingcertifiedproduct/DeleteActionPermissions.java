package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {
    private PendingCertifiedProductDAO pcpDao;

    @Autowired
    public DeleteActionPermissions(final PendingCertifiedProductDAO pcpDao) {
        this.pcpDao = pcpDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    @Transactional
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long pcpId = (Long) obj;
            try {
                Long acbId = pcpDao.findAcbIdById(pcpId);
                if (acbId != null) {
                    return isAcbValidForCurrentUser(acbId);
                } else {
                    //cannot determine acb for unknown reason
                    return false;
                }
            } catch (EntityRetrievalException ex) {
                //bad pcp id passed in
                return false;
            }
        } else {
            return false;
        }
    }
}
