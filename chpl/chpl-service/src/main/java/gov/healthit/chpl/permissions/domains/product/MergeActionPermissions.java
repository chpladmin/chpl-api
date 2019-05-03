package gov.healthit.chpl.permissions.domains.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productMergeActionPermissions")
public class MergeActionPermissions extends ActionPermissions {

    @Autowired
    private ProductDAO productDAO;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                // Need to make sure developer for products to be merged is active
                List<Long> productIds = (List<Long>) obj;
                // All products will have the same developer, so we only need to check the first one
                ProductDTO productDTO = productDAO.getById(productIds.get(0));
                return getResourcePermissions().isDeveloperActive(productDTO.getDeveloperId());
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

}
