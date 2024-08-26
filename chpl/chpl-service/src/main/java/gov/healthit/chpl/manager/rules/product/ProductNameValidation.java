package gov.healthit.chpl.manager.rules.product;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
        currentProductsForDeveloper = currentProductsForDeveloper.stream()
            .filter(currProd -> !currProd.getId().equals(context.getProduct().getId()))
            .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(context.getProductsBeingMerged())) {
            currentProductsForDeveloper = currentProductsForDeveloper.stream()
                    .filter(currProd -> !context.getProductsBeingMerged().contains(currProd.getId()))
                    .collect(Collectors.toList());
        }

        boolean currentProductWithSameName = currentProductsForDeveloper.stream()
            .filter(currProd -> currProd.getName().equalsIgnoreCase(updatedProductName))
            .findAny().isPresent();
        if (currentProductWithSameName) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.duplicateName", updatedProductName));
            return false;
        }
        return true;
    }
}
