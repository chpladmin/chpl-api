package gov.healthit.chpl.permissions.domains.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productUpdateOwnershipActionPermissions")
public class UpdateOwnershipActionPermissions extends ActionPermissions {

    private ProductDAO productDao;

    @Autowired
    public UpdateOwnershipActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao,
            ProductDAO productDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        this.productDao = productDao;
    }

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
                Product originalProduct = getProduct(product.getId());
                return doesCurrentUserHaveAccessToAllOfProductListings(originalProduct.getId());
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
