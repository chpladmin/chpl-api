package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.fuzzyMatching.FuzzyChoicesManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ProductReviewerTest {
    private static final String MISSING_PRODUCT = "A product name is required.";
    private static final String PRODUCT_FUZZY_MATCH = "This listing has a product name of '%s', but the developer %s has a similarly named product '%s'. Should the listing belong to that product instead?";

    private FuzzyChoicesManager fuzzyChoicesManager;
    private ProductDAO productDao;
    private ErrorMessageUtil errorMessageUtil;
    private ProductReviewer reviewer;

    @Before
    public void setup() {
        fuzzyChoicesManager = Mockito.mock(FuzzyChoicesManager.class);
        productDao = Mockito.mock(ProductDAO.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingProduct")))
            .thenReturn(MISSING_PRODUCT);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.product.fuzzyMatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PRODUCT_FUZZY_MATCH, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new ProductReviewer(fuzzyChoicesManager, productDao, errorMessageUtil);
    }

    @Test
    public void review_productMissing_HasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_PRODUCT));
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_productExists_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("Test")
                        .id(1L)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_developerExistsNewProductNoCloseMatches_noWarning() {
        List<Product> products = Stream.of(Product.builder().id(2L).name("Prod 1").build(),
                Product.builder().id(3L).name("Prod 2").build())
                .collect(Collectors.toList());

        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(1L)))
            .thenReturn(products);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("Test"), ArgumentMatchers.anyList()))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .id(1L)
                        .name("Dev Test")
                        .build())
                .product(Product.builder()
                        .name("Test")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_developerExistsNewProductHasCloseMatch_hasWarning() {
        List<Product> products = Stream.of(Product.builder().id(2L).name("Prod 1").build(),
                Product.builder().id(3L).name("ProdTest").build())
                .collect(Collectors.toList());

        Mockito.when(productDao.getByDeveloper(ArgumentMatchers.eq(1L)))
            .thenReturn(products);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("Prod Test"), ArgumentMatchers.anyList()))
            .thenReturn("ProdTest");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .id(1L)
                        .name("Dev Test")
                        .build())
                .product(Product.builder()
                        .name("Prod Test")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
    }
}
