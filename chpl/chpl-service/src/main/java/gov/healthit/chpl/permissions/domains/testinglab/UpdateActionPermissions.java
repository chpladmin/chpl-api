package gov.healthit.chpl.permissions.domains.testinglab;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("testingLabUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof TestingLab)) {
            return false;
        }

        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

}
