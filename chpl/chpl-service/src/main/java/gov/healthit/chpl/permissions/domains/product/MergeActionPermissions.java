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
                // Need to make sure all developers for products to be merged are active
                List<Long> productIds = (List<Long>) obj;
                for (Long productId : productIds) {
                    ProductDTO productDTO = productDAO.getById(productId);
                    if (!getResourcePermissions().isDeveloperActive(productDTO.getDeveloperId())) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

}
