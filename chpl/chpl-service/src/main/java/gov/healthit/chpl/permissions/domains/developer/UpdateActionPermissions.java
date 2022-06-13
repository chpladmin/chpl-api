package gov.healthit.chpl.permissions.domains.developer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
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
        if (!(obj instanceof Developer)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            // The user can only update if the developer is currently active
            // Need to get the current status for the developer from the DB...
            Developer originalDeveloper = (Developer) obj;
            try {
                Developer currentDeveloper = developerDAO.getById(originalDeveloper.getId());
                return currentDeveloper != null && currentDeveloper.getStatus() != null
                        && currentDeveloper.getStatus().getStatus().equals(DeveloperStatusType.Active.toString());
            } catch (EntityRetrievalException e) {
                return false;
            }
        } else {
            return false;
        }
    }

}
