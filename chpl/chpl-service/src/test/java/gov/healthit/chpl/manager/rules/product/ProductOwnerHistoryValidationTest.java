package gov.healthit.chpl.manager.rules.product;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ProductOwnerHistoryValidationTest {
    private static final String PRODUCT_OWNER_HISTORY_INVALID = "Only one product owner change is allowed per day.";

    private ErrorMessageUtil msgUtil;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.ownerStatusHistory.notSameDay")))
            .thenReturn(PRODUCT_OWNER_HISTORY_INVALID);
    }

    @Test
    public void review_nullProductOwnerHistory_noError() {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(null)
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation();
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_emptyProductOwnerHistory_noError() {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(new ArrayList<ProductOwner>())
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation();
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerHistoryOneEntry_noError() {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")))
                                .toList())
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation();
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerHistoryTwoEntriesDifferentDays_noError() {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .ownerHistory(Stream.of(
                                getOwnerHistoryEntry(1L, 1L, "owner 1", LocalDate.parse("2022-01-01")),
                                getOwnerHistoryEntry(2L, 2L, "owner 2", LocalDate.parse("2022-02-01")))
                                .toList())
                        .build())

                .build();

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation();
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerHistoryTwoEntriesSameDay_hasError() {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
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

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation();
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_HISTORY_INVALID));
    }

    @Test
    public void review_productOwnerHistoryMultipleEntriesWithTwoOnSameDay_hasError() {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
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

        ProductOwnerHistoryValidation validation = new ProductOwnerHistoryValidation();
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_HISTORY_INVALID));
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
