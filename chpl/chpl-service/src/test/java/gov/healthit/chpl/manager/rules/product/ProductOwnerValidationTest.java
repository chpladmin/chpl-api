package gov.healthit.chpl.manager.rules.product;

import static org.junit.Assert.assertFalse;
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
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ProductOwnerValidationTest {
    private static final String PRODUCT_OWNER_MISSING = "A product owner is required.";
    private static final String PRODUCT_OWNER_DOES_NOT_EXIST = "An owner with ID %s was specified for the product but that developer does not exist.";
    private static final String PRODUCT_OWNER_STATUS_DOES_NOT_EXIST = "The product '%s' cannot be created since the status of developer '%s' cannot be determined.";
    private static final String PRODUCT_OWNER_STATUS_NOT_ACTIVE = "The product owner must be Active. Currently, the product owner has a status of '%s'.";
    private static final String PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS = "%s has no other products so this product cannot be transferred. A developer may not have 0 products.";

    private ProductDAO productDao;
    private ErrorMessageUtil msgUtil;

    @Before
    public void setup() throws EntityRetrievalException {
        productDao = Mockito.mock(ProductDAO.class);
        Mockito.when(productDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(Product.builder().build());

        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.ownerRequired")))
            .thenReturn(PRODUCT_OWNER_MISSING);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.ownerMustExist"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PRODUCT_OWNER_DOES_NOT_EXIST, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.ownerStatusMustExist"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PRODUCT_OWNER_STATUS_DOES_NOT_EXIST, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("product.ownerMustBeActive"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PRODUCT_OWNER_STATUS_NOT_ACTIVE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("product.ownerHistory.cannotTransferDevelopersOnlyProduct"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS, i.getArgument(1), ""));
    }

    @Test
    public void review_nullProductOwner_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(Mockito.mock(DeveloperDAO.class))
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(null)
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, Mockito.mock(ResourcePermissions.class));
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_MISSING));
    }

    @Test
    public void review_nullProductOwnerId_hasError() {
        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(Mockito.mock(DeveloperDAO.class))
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(null)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, Mockito.mock(ResourcePermissions.class));
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(PRODUCT_OWNER_MISSING));
    }

    @Test
    public void review_productOwnerDoesNotExist_hasError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(null);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, Mockito.mock(ResourcePermissions.class));
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_DOES_NOT_EXIST, "1")));
    }

    @Test
    public void review_productOwnerExistsNullStatus_hasError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(null)
                .build());

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, Mockito.mock(ResourcePermissions.class));
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_STATUS_DOES_NOT_EXIST, "name", "developer 1")));
    }

    @Test
    public void review_productOwnerExistsEmptyStatus_hasError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(new ArrayList<DeveloperStatusEvent>())
                .build());

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, Mockito.mock(ResourcePermissions.class));
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_STATUS_DOES_NOT_EXIST, "name", "developer 1")));
    }

    @Test
    public void review_productOwnerChangesAndNoOtherProducts_hasError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(Stream.of(
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-01-01")))
                        .toList())
                .build());

        Mockito.when(productDao.getById(ArgumentMatchers.eq(1L)))
            .thenReturn(Product.builder()
                    .id(1L)
                    .name("name")
                    .owner(Developer.builder()
                            .id(2L)
                            .name("old dev")
                            .build())
                    .build());

        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(2L)))
            .thenReturn(Stream.of(Product.builder()
                    .id(1L)
                    .name("name")
                    .build()).toList());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .id(1L)
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .name("developer 1")
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, resourcePermissions);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_HISTORY_OWNER_NO_PRODUCTS, "old dev")));
    }

    @Test
    public void review_productOwnerChangesAndHasOtherProducts_noError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(Stream.of(
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-01-01")))
                        .toList())
                .build());

        Mockito.when(productDao.getById(ArgumentMatchers.eq(1L)))
            .thenReturn(Product.builder()
                    .id(1L)
                    .name("name")
                    .owner(Developer.builder()
                            .id(2L)
                            .name("old dev")
                            .build())
                    .build());

        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(2L)))
            .thenReturn(Stream.of(Product.builder()
                    .id(1L)
                    .name("name")
                    .build(),
                    Product.builder()
                    .id(3L)
                    .name("other product")
                    .build()).toList());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .id(1L)
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .name("developer 1")
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, resourcePermissions);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
    }

    @Test
    public void review_productOwnerExistsSuspendedCurrentStatus_userIsAcb_hasError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(Stream.of(
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-01-01")),
                        getDeveloperStatusEvent(2L, "Suspended by ONC", LocalDate.parse("2022-10-01")))
                        .toList())
                .build());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, resourcePermissions);
        boolean isValid = validation.isValid(context);
        assertFalse(isValid);
        assertTrue(validation.getMessages().contains(String.format(PRODUCT_OWNER_STATUS_NOT_ACTIVE, "Suspended by ONC")));
    }

    @Test
    public void review_productOwnerExistsSuspendedCurrentStatus_userIsOnc_noError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(Stream.of(
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-01-01")),
                        getDeveloperStatusEvent(2L, "Suspended by ONC", LocalDate.parse("2022-10-01")))
                        .toList())
                .build());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, resourcePermissions);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerExistsSuspendedCurrentStatus_userIsAdmin_noError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(Stream.of(
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-01-01")),
                        getDeveloperStatusEvent(2L, "Suspended by ONC", LocalDate.parse("2022-10-01")))
                        .toList())
                .build());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, resourcePermissions);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerExistsActiveCurrentStatus_userIsAcb_noError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(Stream.of(
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-01-01")),
                        getDeveloperStatusEvent(2L, "Suspended by ONC", LocalDate.parse("2022-10-01")),
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-10-05")))
                        .toList())
                .build());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, resourcePermissions);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    @Test
    public void review_productOwnerExistsActiveOnlyStatus_userIsAcb_noError() throws EntityRetrievalException {
        DeveloperDAO devDao = Mockito.mock(DeveloperDAO.class);
        Mockito.when(devDao.getById(ArgumentMatchers.eq(1L))).thenReturn(Developer.builder()
                .id(1L)
                .name("developer 1")
                .statusEvents(Stream.of(
                        getDeveloperStatusEvent(1L, "Active", LocalDate.parse("2022-01-01")))
                        .toList())
                .build());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ProductValidationContext context = ProductValidationContext.builder()
                .developerDao(devDao)
                .errorMessageUtil(msgUtil)
                .product(Product.builder()
                        .name("name")
                        .owner(Developer.builder()
                                .id(1L)
                                .build())
                        .build())

                .build();

        ProductOwnerValidation validation = new ProductOwnerValidation(productDao, resourcePermissions);
        boolean isValid = validation.isValid(context);
        assertTrue(isValid);
        assertTrue(CollectionUtils.isEmpty(validation.getMessages()));
    }

    private DeveloperStatusEvent getDeveloperStatusEvent(Long statusEventId, String statusName, LocalDate statusDate) {
        return DeveloperStatusEvent.builder()
                .id(statusEventId)
                .status(DeveloperStatus.builder()
                        .status(statusName)
                        .build())
                .statusDate(DateUtil.toDate(statusDate))
            .build();
    }
}
