package gov.healthit.chpl.permissions.domains.developer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {
    @Autowired
    private DeveloperDAO developerDAO;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof DeveloperDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            // The user can only update if the developer is currently active
            // Need to get the current status for the developer from the DB...
            DeveloperDTO originalDeveloper = (DeveloperDTO) obj;
            try {
                DeveloperDTO currentDeveloper = developerDAO.getById(originalDeveloper.getId());
                if (currentDeveloper != null && currentDeveloper.getStatus() != null && currentDeveloper.getStatus()
                        .getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
                    return true;
                } else {
                    return false;
                }
            } catch (EntityRetrievalException e) {
                return false;
            }
        } else {
            return false;
        }
    }

}
