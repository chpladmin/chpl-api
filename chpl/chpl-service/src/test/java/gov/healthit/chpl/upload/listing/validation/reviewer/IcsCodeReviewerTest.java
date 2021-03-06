package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class IcsCodeReviewerTest {
    private static final String CODE_FALSE_VALUE_TRUE_MISMATCH =
            "The unique id indicates the product does not have ICS but the value for Inherited Certification Status is true.";
    private static final String CODE_TRUE_VALUE_FALSE_MISMATCH =
            "The unique id indicates the product does have ICS but the value for Inherited Certification Status is false.";

    private ErrorMessageUtil errorMessageUtil;
    private IcsCodeReviewer reviewer;

    @Before
    public void setup() {
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil();
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage("listing.icsCodeTrueValueFalse"))
            .thenReturn(CODE_TRUE_VALUE_FALSE_MISMATCH);
        Mockito.when(errorMessageUtil.getMessage("listing.icsCodeFalseValueTrue"))
            .thenReturn(CODE_FALSE_VALUE_TRUE_MISMATCH);
        reviewer = new IcsCodeReviewer(chplProductNumberUtil, errorMessageUtil);
    }

    @Test
    public void review_nullChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidFormatOfChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("bad.format")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nonzeroIcsCodeAndIcsBooleanFalse_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.01.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_TRUE_VALUE_FALSE_MISMATCH));
    }

    @Test
    public void review_zeroIcsCodeAndIcsBooleanTrue_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_FALSE_VALUE_TRUE_MISMATCH));
    }

    @Test
    public void review_notNumberIcsCodeAndIcsBooleanNull_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.AB.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_notNumberIcsCodeAndIcsBooleanTrue_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.AB.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_notNumberIcsCodeAndIcsBooleanFalse_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.AB.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nonzeroIcsCodeAndIcsBooleanNull_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.02.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_TRUE_VALUE_FALSE_MISMATCH));
    }

    @Test
    public void review_nonzeroIcsCodeAndIcsBooleanTrue_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.02.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_zeroIcsCodeAndIcsBooleanFalse_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210102")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
