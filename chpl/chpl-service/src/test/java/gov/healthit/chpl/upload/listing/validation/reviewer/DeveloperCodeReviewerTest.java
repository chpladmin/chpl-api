package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class DeveloperCodeReviewerTest {
    private static final String CODE_XXXX = "The CHPL Product Number has a new developer code of 'XXXX' but the developer already exists in the system.";
    private static final String CODE_NOT_XXXX = "The CHPL Product Number has a developer code of '%s' but the developer does not yet exist in the system. To indicate a new developer the CHPL Product Number should use the code 'XXXX'.";
    private static final String CODE_MISMATCH = "The developer code from the CHPL Product Number %s does not match the code of the responsible developer %s.";
    private static final String CODE_MISSING = "A developer code is required.";

    private ErrorMessageUtil errorMessageUtil;
    private DeveloperCodeReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new DeveloperCodeReviewer(new ChplProductNumberUtil(), new ValidationUtils(), errorMessageUtil);
    }

    @Test
    public void review_nullDeveloperValidCodeInChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullDeveloperEmptyCodeInChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04..WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyCodeInListingValidCodeInChplProductNumber_hasError() {
        Mockito.when(errorMessageUtil.getMessage("listing.missingDeveloperCode"))
            .thenReturn(CODE_MISSING);

        Developer dev = Developer.builder()
                .id(1L)
                .developerCode("")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .developer(dev)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_MISSING));
    }

    @Test
    public void review_nullCodeInListingValidCodeInChplProductNumber_hasError() {
        Mockito.when(errorMessageUtil.getMessage("listing.missingDeveloperCode"))
            .thenReturn(CODE_MISSING);

        Developer dev = Developer.builder()
                .id(1L)
                .developerCode(null)
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .developer(dev)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_MISSING));
    }

    @Test
    public void review_legacyChplProductNumberWithDeveloper_noError() {
        Developer dev = Developer.builder()
                .developerCode(null)
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .developer(dev)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberNullDeveloper_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_devCodeXXXXListingHasDeveloperId_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.shouldNotHaveXXXXCode")))
            .thenReturn(CODE_XXXX);

        Developer dev = Developer.builder()
                .id(1L)
                .name("Test")
                .developerCode("1234")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.99.04.XXXX.WEBe.06.00.1.210101")
                .developer(dev)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_XXXX));
    }

    @Test
    public void review_developerCodeNotXXXXListingHasNewDeveloper_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.shouldHaveXXXXCode"), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(CODE_NOT_XXXX, i.getArgument(1), ""));

        Developer dev = Developer.builder()
                .name("Test")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.01.04.2526.WEBe.06.00.1.210101")
                .developer(dev)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CODE_NOT_XXXX, "2526", "")));
    }

    @Test
    public void review_mismatchedDeveloperCode_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.developerCodeMismatch"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CODE_MISMATCH, i.getArgument(1), i.getArgument(2)));

        Developer dev = Developer.builder()
                .id(1L)
                .developerCode("1234")
                .name("Test")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .developer(dev)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CODE_MISMATCH, "2526", "1234")));
    }

    @Test
    public void review_mismatchedDeveloperCodeInvalidChplProductNumber_noError() {
        Developer dev = Developer.builder()
                .id(1L)
                .developerCode("1234")
                .name("Test")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.??LJ.WEBe.06.00.1.210101")
                .developer(dev)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validXXXXCodeWithMatchingNewDeveloper_noError() {
        Developer dev = Developer.builder()
                .name("Test")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(dev)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_matchingDeveloperCode_noError() {
        Developer dev = Developer.builder()
                .id(1L)
                .developerCode("1234")
                .name("Test")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.99.04.1234.WEBe.06.00.1.210101")
                .developer(dev)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
