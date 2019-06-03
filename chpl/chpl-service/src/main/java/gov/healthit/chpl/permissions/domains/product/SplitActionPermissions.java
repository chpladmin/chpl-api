package gov.healthit.chpl.permissions.domains.product;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof ProductDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                ProductDTO dto = (ProductDTO) obj;
                if (getResourcePermissions().isDeveloperActive(dto.getDeveloperId())) {
                    return doesCurrentUserHaveAccessToAllOfDevelopersListings(dto.getDeveloperId());
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
