package gov.healthit.chpl.manager.rules.product;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ProductNameValidation extends ValidationRule<ProductValidationContext> {

    @Override
    public boolean isValid(ProductValidationContext context) {
        if (StringUtils.isBlank(context.getProduct().getName())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("product.nameRequired"));
            return false;
        }
        return true;
    }
}
