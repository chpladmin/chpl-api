package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ProductReviewerTest {
    private static final String MISSING_PRODUCT = "A product name is required.";

    private ErrorMessageUtil errorMessageUtil;
    private ProductReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingProduct")))
            .thenReturn(MISSING_PRODUCT);
        reviewer = new ProductReviewer(errorMessageUtil);
    }

    @Test
    public void review_productMissing_HasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_PRODUCT));
    }

    @Test
    public void review_productExists_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("Test")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }
}
