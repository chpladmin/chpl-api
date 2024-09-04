package gov.healthit.chpl.manager.rules.product;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ProductNameValidationTest {
    private static final String PRODUCT_NAME_MISSING = "A product name is required.";
    private static final String PRODUCT_NAME_DUPLICATE = "The product name %s already exists.";

    private ProductDAO productDao;
    private ErrorMessageUtil msgUtil;

    @Before
    public void setup() {
        productDao = Mockito.mock(ProductDAO.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.anyLong()))
            .thenReturn(new ArrayList<Product>());

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.nameRequired")))
            .thenReturn(PRODUCT_NAME_MISSING);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.duplicateName"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PRODUCT_NAME_DUPLICATE, i.getArgument(1), ""));
    }

    @Test
    public void review_nullProductName_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .productDao(productDao)
                .developerId(1L)
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
                .productDao(productDao)
                .developerId(1L)
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
                .productDao(productDao)
                .developerId(1L)
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
                .productDao(productDao)
                .developerId(1L)
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

    @Test
    public void review_duplicateProductName_hasError() {
        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.anyLong()))
            .thenReturn(Stream.of(Product.builder()
                    .id(2L)
                    .name("dupname")
                    .build()).toList());

        ProductValidationContext context = ProductValidationContext.builder()
                .productDao(productDao)
                .developerId(1L)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .id(1L)
                        .name("dupname")
                        .build())

                .build();

        ProductNameValidation validation = new ProductNameValidation();
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_NAME_DUPLICATE, "dupname")));
    }
}
