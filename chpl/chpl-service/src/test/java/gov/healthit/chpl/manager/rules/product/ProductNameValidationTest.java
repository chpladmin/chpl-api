package gov.healthit.chpl.manager.rules.product;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ProductNameValidationTest {
    private static final String PRODUCT_NAME_MISSING = "A product name is required.";

    private ErrorMessageUtil msgUtil;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.nameRequired")))
            .thenReturn(PRODUCT_NAME_MISSING);
    }

    @Test
    public void review_nullProductName_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(Mockito.mock(DeveloperDAO.class))
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name(null)
                        .build())

                .build();

        ProductNameValidation validation = new ProductNameValidation();
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_NAME_MISSING));
    }

    @Test
    public void review_emptyProductName_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(Mockito.mock(DeveloperDAO.class))
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("")
                        .build())

                .build();

        ProductNameValidation validation = new ProductNameValidation();
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_NAME_MISSING));
    }

    @Test
    public void review_blankProductName_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(Mockito.mock(DeveloperDAO.class))
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("   ")
                        .build())

                .build();

        ProductNameValidation validation = new ProductNameValidation();
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_NAME_MISSING));
    }

    @Test
    public void review_hasProductName_noError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(Mockito.mock(DeveloperDAO.class))
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .build())

                .build();

        ProductNameValidation validation = new ProductNameValidation();
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }
}
