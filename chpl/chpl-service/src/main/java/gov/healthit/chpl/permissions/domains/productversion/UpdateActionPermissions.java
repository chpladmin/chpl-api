package gov.healthit.chpl.permissions.domains.productversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productVersionUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Autowired
    private DeveloperDAO developerDao;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ProductVersionDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                ProductVersionDTO dto = (ProductVersionDTO) obj;
                return getResourcePermissions().isDeveloperActive(dto.getDeveloperId());
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
