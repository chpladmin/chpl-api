package gov.healthit.chpl.manager.rules.product;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ProductOwnerHistoryValidationTest {
    private static final String PRODUCT_OWNER_HISTORY_DATE_INVALID = "Only one product owner change is allowed per day.";
    private static final String PRODUCT_OWNER_HISTORY_OWNER_INVALID = "The same developer cannot have two contiguous product ownership history entries.";
    private static final String PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS = "%s has no other products so this product cannot be transferred. A developer may not have 0 products.";

    private DeveloperDAO devDao;
    private ProductDAO productDao;
    private ErrorMessageUtil msgUtil;

    @Before
    public void setup() throws EntityRetrievalException {
        devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(Developer.builder()
                    .id(1L)
                    .name("owner 1")
                    .deleted(false)
                    .build());

        productDao = Mockito.mock(ProductDAO.class);
        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.anyLong()))
            .thenReturn(Stream.of(Product.builder().id(500L).build(), Product.builder().id(501L).build()).toList());

        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.ownerHistory.notSameDay")))
            .thenReturn(PRODUCT_OWNER_HISTORY_DATE_INVALID);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.ownerHistory.sameOwner")))
            .thenReturn(PRODUCT_OWNER_HISTORY_OWNER_INVALID);
        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("product.ownerHistory.cannotTransferDevelopersOnlyProduct"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS, i.getArgument(1), ""));
    }

    @Test
    public void review_nullProductOwnerHistory_noError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(null)
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_emptyProductOwnerHistory_noError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(new ArrayList<ProductOwner>())
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerHistoryOneEntry_noError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerHistoryTwoEntriesDifferentDays_noError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-02-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerHistoryMostRecentOwnerHasNullProducts_hasError() {
        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(1L)))
            .thenReturn(null);

        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2023-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-02-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS, "owner 1")));
    }

    @Test
    public void review_productOwnerHistoryMostRecentOwnerHasEmptyProducts_hasError() {
        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(1L)))
            .thenReturn(new ArrayList<Product>());

        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2023-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-02-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS, "owner 1")));
    }

    @Test
    public void review_productOwnerHistoryMostRecentOwnerHasOnlyThisProduct_hasError() {
        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(1L)))
            .thenReturn(Stream.of(Product.builder().id(3L).build()).toList());

        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .id(3L)
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2023-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-02-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS, "owner 1")));
    }

    @Test
    public void review_productOwnerHistoryMostRecentOwnerHasOnlyThisProductButIsDeleted_noError() {
        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(1L)))
            .thenReturn(Stream.of(Product.builder().id(3L).build()).toList());

        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(Developer.builder()
                    .id(1L)
                    .name("owner 1")
                    .deleted(true)
                    .build());
        } catch (EntityRetrievalException e) {
            fail(e.getMessage());
        }

        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .id(3L)
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2023-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-02-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
    }

    @Test
    public void review_productOwnerHistoryMostRecentOwnerHasThisAndAnotherProduct_noError() {
        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(1L)))
            .thenReturn(Stream.of(Product.builder().id(3L).build(),
                    Product.builder().id(4L).build()).toList());

        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .id(3L)
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2023-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-02-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerHistoryTwoEntriesSameDay_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-01-01")))
                                .toList())
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_HISTORY_DATE_INVALID));
    }

    @Test
    public void review_productOwnerHistoryMultipleEntriesWithTwoOnSameDay_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")),
                                getOwnerHistoryEntry(3L, 3L, "owner 3", LocalDate.parse("2022-03-03")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-01-01")),
                                getOwnerHistoryEntry(4L, 4L, "owner 4", LocalDate.parse("2022-04-04")))
                                .toList())
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_HISTORY_DATE_INVALID));
    }

    @Test
    public void review_productOwnerHistoryTwoEntriesSameOwner_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")),
                                getOwnerHistoryEntry(2L, 1L, "owner 1", LocalDate.parse("2022-02-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_HISTORY_OWNER_INVALID));
    }

    @Test
    public void review_productOwnerHistoryMultipleEntriesSameOwner_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")),
                                getOwnerHistoryEntry(2L, 1L, "owner 1", LocalDate.parse("2022-02-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-03-01")))
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation(devDao, productDao);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_HISTORY_OWNER_INVALID));
    }

    private ProductOwner getOwnerHistoryEntry(Long historyEventId, Long ownerId, String ownerName, LocalDate transferDay) {
        return ProductOwner.builder()
                .id(historyEventId)
                .developer(Developer.builder()
                        .id(ownerId)
                        .name(ownerName)
                        .build())
                .transferDay(transferDay)
            .build();
    }
}
