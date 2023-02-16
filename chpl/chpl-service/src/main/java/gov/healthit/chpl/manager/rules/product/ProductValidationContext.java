package gov.healthit.chpl.manager.rules.product;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductValidationContext {
    private DeveloperDAO developerDao;
    private Product product;
    private boolean isMergingOwner;
    private ErrorMessageUtil errorMessageUtil;

    public ProductValidationContext(Product product, DeveloperDAO developerDao,
            boolean isMergingOwner,
            ErrorMessageUtil errorMessageUtil) {
        this.product = product;
        this.developerDao = developerDao;
        this.isMergingOwner = isMergingOwner;
        this.errorMessageUtil = errorMessageUtil;
    }
}
