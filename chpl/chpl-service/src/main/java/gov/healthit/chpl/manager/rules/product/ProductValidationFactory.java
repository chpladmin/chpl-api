package gov.healthit.chpl.manager.rules.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

@Component
public class ProductValidationFactory {
    public static final String NAME = "NAME";
    public static final String OWNER = "OWNER";
    public static final String OWNER_HISTORY = "OWNER_HISTORY";

    private ProductDAO productDao;
    private DeveloperDAO developerDao;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public ProductValidationFactory(DeveloperDAO developerDao, ProductDAO productDao,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        this.developerDao = developerDao;
        this.productDao = productDao;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    public ValidationRule<ProductValidationContext> getRule(String name) {
        switch (name) {
        case NAME:
            return new ProductNameValidation();
        case OWNER:
            return new ProductOwnerValidation(developerDao, productDao, resourcePermissionsFactory);
        case OWNER_HISTORY:
            return new ProductOwnerHistoryValidation(developerDao, productDao);
        default:
            return null;
        }
    }
}
