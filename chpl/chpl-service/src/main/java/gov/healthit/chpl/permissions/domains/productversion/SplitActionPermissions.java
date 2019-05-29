package gov.healthit.chpl.permissions.domains.productversion;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productVersionSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof ProductVersionDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                ProductVersionDTO versionDto = (ProductVersionDTO) obj;
                if (getResourcePermissions().isDeveloperActive(versionDto.getDeveloperId())) {
                    return doesCurrentUserHaveAccessToAllOfDevelopersListings(versionDto.getDeveloperId());
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
