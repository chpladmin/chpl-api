package gov.healthit.chpl.manager.rules.product;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ProductNameValidation extends ValidationRule<ProductValidationContext> {

    @Override
    public boolean isValid(ProductValidationContext context) {
        String updatedProductName = context.getProduct().getName();
        if (StringUtils.isBlank(updatedProductName)) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.nameRequired"));
            return false;
        }
        List<Product> currentProductsForDeveloper = context.getProductDao().getByDeveloper(context.getDeveloperId());
        boolean currentProductWithSameName = currentProductsForDeveloper.stream()
            .filter(currProd -> currProd.getName().equalsIgnoreCase(updatedProductName)
                    && !currProd.getId().equals(context.getProduct().getId()))
            .findAny().isPresent();
        if (currentProductWithSameName) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.duplicateName", updatedProductName));
            return false;
        }
        return true;
    }
}
