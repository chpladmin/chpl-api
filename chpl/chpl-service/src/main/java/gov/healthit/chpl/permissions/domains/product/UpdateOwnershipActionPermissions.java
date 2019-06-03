package gov.healthit.chpl.permissions.domains.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dto.ProductDTO;
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
        if (!(obj instanceof ProductDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                ProductDTO dto = (ProductDTO) obj;
                // Get the original ProductDTO, since the one passed in has the "new" developer id
                ProductDTO originalProductDto = getProductDTO(dto.getId());
                if (getResourcePermissions().isDeveloperActive(originalProductDto.getDeveloperId())) {
                    return doesCurrentUserHaveAccessToAllOfProductListings(originalProductDto.getId());
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

    private ProductDTO getProductDTO(final Long productId) throws EntityRetrievalException {
        return productDao.getById(productId);
    }
}
