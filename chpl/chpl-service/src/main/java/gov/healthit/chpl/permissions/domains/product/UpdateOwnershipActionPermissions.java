package gov.healthit.chpl.permissions.domains.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productUpdateOwnershipActionPermissions")
public class UpdateOwnershipActionPermissions extends ActionPermissions {

    @Autowired
    private ProductDAO productDao;

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
            try {
                Product product = (Product) obj;
                // Get the original Product, since the one passed in has the "new" developer id
                Product originalProduct = getProduct(product.getProductId());
                if (getResourcePermissions().isDeveloperActive(originalProduct.getOwner().getDeveloperId())) {
                    return doesCurrentUserHaveAccessToAllOfProductListings(originalProduct.getProductId());
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

    private Product getProduct(Long productId) throws EntityRetrievalException {
        return productDao.getById(productId);
    }
}
