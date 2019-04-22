package gov.healthit.chpl.permissions.domains.product;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ProductDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ProductDTO dto = (ProductDTO) obj;
            return getResourcePermissions().isDeveloperActive(dto.getDeveloperId());
        } else {
            return false;
        }
    }
}
