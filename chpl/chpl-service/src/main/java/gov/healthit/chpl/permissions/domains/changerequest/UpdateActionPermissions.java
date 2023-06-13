package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestUpdateRequest;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {
    private ChangeRequestDAO changeRequestDAO;

    @Autowired
    public UpdateActionPermissions(final ChangeRequestDAO changeRequestDAO) {
        this.changeRequestDAO = changeRequestDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof ChangeRequestUpdateRequest)) {
                return false;
            } else if (getResourcePermissions().isUserRoleOnc() || getResourcePermissions().isUserRoleAdmin()) {
                return true;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
                ChangeRequest cr = changeRequestDAO.get((((ChangeRequestUpdateRequest) obj).getChangeRequest()).getId());
                return isCurrentAcbUserAssociatedWithDeveloper(cr.getDeveloper().getId());
            } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
                ChangeRequest cr = changeRequestDAO.get((((ChangeRequestUpdateRequest) obj).getChangeRequest()).getId());
                return isDeveloperValidForCurrentUser(cr.getDeveloper().getId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
