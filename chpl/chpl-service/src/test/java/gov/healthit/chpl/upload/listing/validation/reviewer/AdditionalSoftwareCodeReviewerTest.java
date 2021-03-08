package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AdditionalSoftwareCodeReviewerTest {
    private static final String CODE_0_MISMATCH = "The unique id indicates the product does not have additional software but some is specified in the upload file.";
    private static final String CODE_1_MISMATCH = "The unique id indicates the product does have additional software but none is specified in the upload file.";

    private ErrorMessageUtil errorMessageUtil;
    private AdditionalSoftwareCodeReviewer reviewer;

    @Before
    public void setup() {
        ChplProductNumberUtil chplProductNumberUtil =  new ChplProductNumberUtil();
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        reviewer = new AdditionalSoftwareCodeReviewer(chplProductNumberUtil, errorMessageUtil);
    }

    @Test
    public void review_nullChplProductNumber_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyChplProductNumber_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidFormatOfChplProductNumber_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("bad.format")
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumber_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_falseAdditionalSoftwareCodeNoCriteria_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.0.210102")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_falseAdditionalSoftwareCodeNoAdditionalSoftware_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.0.210102")
                .certificationResult(CertificationResult.builder()
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_trueAdditionalSoftwareCodeWithAdditionalSoftware_noError() throws ParseException {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .grouping("A")
                .name("Windows")
                .version("2020")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210102")
                .certificationResult(CertificationResult.builder()
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_trueAdditionalSoftwareCodeNoAdditionalSoftware_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.additionalSoftwareCode1Mismatch"))
            .thenReturn(CODE_1_MISMATCH);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210102")
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_1_MISMATCH));
    }

    @Test
    public void review_falseAdditionalSoftwareCodeWithAdditionalSoftware_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.additionalSoftwareCode0Mismatch"))
            .thenReturn(CODE_0_MISMATCH);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .grouping("A")
                .name("Windows")
                .version("2020")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.0.210102")
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CODE_0_MISMATCH));
    }
}
