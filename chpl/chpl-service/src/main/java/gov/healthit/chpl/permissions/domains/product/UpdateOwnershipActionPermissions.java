package gov.healthit.chpl.permissions.domains.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productUpdateOwnershipActionPermissions")
public class UpdateOwnershipActionPermissions extends ActionPermissions {

    @Autowired
    private DeveloperDAO developerDao;

    @Autowired
    private CertifiedProductDAO certifiedProductDao;

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
            if (getResourcePermissions().isDeveloperActive(dto.getDeveloperId())) {
                return doesCurrentUserHaveAccessToAllOfDevelopersListings(dto.getDeveloperId());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
