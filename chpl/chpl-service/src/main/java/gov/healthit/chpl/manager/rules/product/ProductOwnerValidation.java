package gov.healthit.chpl.manager.rules.product;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProductOwnerValidation extends ValidationRule<ProductValidationContext> {
    private DeveloperDAO developerDao;
    private ProductDAO productDao;
    private ResourcePermissions resourcePermissions;

    public ProductOwnerValidation(DeveloperDAO developerDao, ProductDAO productDao,
            ResourcePermissions resourcePermissions) {
        this.developerDao = developerDao;
        this.productDao = productDao;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(ProductValidationContext context) {
        if (ObjectUtils.isEmpty(context.getProduct().getOwner())
                || ObjectUtils.isEmpty(context.getProduct().getOwner().getId())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.ownerRequired"));
            return false;
        }
        Long developerId = context.getProduct().getOwner().getId();
        Developer productOwner = null;
        try {
            productOwner = developerDao.getById(developerId);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Product owner with ID " + developerId + " may not exist.", ex);
        }

        if (productOwner == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.ownerMustExist", developerId.toString()));
            return false;
        }
        DeveloperStatus currDevStatus = productOwner.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.ownerStatusMustExist",
                    context.getProduct().getName(),
                    productOwner.getName()));
            return false;
        } else if (!currDevStatus.getStatus().equals(DeveloperStatusType.Active.toString())
                && !isUserAllowedToActOnInactiveDeveloper()) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.ownerMustBeActive",
                    currDevStatus.getStatus()));
            return false;
        }

        if (!context.isMergingOwner()) {
            //the owner could be changing without an entry in the history
            Product existingProduct = getExistingProduct(context.getProduct().getId());
            if (ownerIsChanging(existingProduct, context.getProduct())
                    && developerHasNoOtherProducts(existingProduct.getOwner().getId(), context.getProduct().getId())) {
                Developer owner = getOwner(existingProduct.getOwner().getId());
                if (owner != null && !owner.getDeleted()) {
                    getMessages().add(context.getErrorMessageUtil().getMessage(
                            "product.ownerHistory.cannotTransferDevelopersOnlyProduct",
                            existingProduct.getOwner().getName()));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isUserAllowedToActOnInactiveDeveloper() {
        return resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc();
    }

    private Product getExistingProduct(Long productId) {
        Product product = null;
        try {
            product = productDao.getById(productId);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("No product found with ID " + productId);
        }
        return product;
    }

    private boolean ownerIsChanging(Product existingProduct, Product contextProduct) {
        if (existingProduct == null) {
            return false;
        }
        return !existingProduct.getOwner().getId().equals(contextProduct.getOwner().getId());
    }

    private boolean developerHasNoOtherProducts(Long developerId, Long productId) {
        List<Product> developerProducts = productDao.getByDeveloper(developerId);
        if (CollectionUtils.isEmpty(developerProducts)) {
            return true;
        } else if (developerProducts.size() == 1) {
            return developerProducts.get(0).getId().equals(productId);
        }
        return false;
    }

    private Developer getOwner(Long developerId) {
        Developer owner = null;
        try {
            owner = developerDao.getById(developerId, true);
        } catch (Exception ex) {
            LOGGER.error("No developer found (including deleted) with ID : " + developerId);
        }
        return owner;
    }
}
