package gov.healthit.chpl.permissions.domains.product;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Product)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Product dto = (Product) obj;
            return getResourcePermissions().isDeveloperActive(dto.getOwner().getId());
        } else {
            return false;
        }
    }
}
