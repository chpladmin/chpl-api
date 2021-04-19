package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FieldLengthReviewerTest {
    private static final String FIELD_TOO_LONG = "You have exceeded the max length, %s characters, for the %s. You will need to correct this error before you can confirm. Current value: '%s'";

    private ErrorMessageUtil errorMessageUtil;
    private MessageSource messageSource;
    private FieldLengthReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        messageSource = Mockito.mock(MessageSource.class);

        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.developerName"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.developerName.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "developer name", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.productName"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.productName.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "product name", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.productVersion"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.productVersion.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "product version", "placeholder"));
        reviewer = new FieldLengthReviewer(errorMessageUtil, messageSource);
    }

    @Test
    public void review_nullDeveloperName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name(null)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyDeveloperName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name("")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortDeveloperName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name("test")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longDeveloperName_hasError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name("testtesttesttesttesttest")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "developer name", "placeholder")));
    }

    @Test
    public void review_nullProductName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name(null)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyProductName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortProductName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("test")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longProductName_hasError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("testtesttesttesttesttesttest")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "product name", "placeholder")));
    }

    @Test
    public void review_nullVersionName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version(null)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyVersionName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version("")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortVersionName_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version("01")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longVersionName_hasError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version("1234567890.0987654321")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "product version", "placeholder")));
    }
}
