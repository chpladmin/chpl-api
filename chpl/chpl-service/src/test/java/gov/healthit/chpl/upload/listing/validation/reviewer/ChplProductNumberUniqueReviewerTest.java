package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ChplProductNumberUniqueReviewerTest {
    private static final String CHPL_NUMBER_NOT_UNIQUE = "The expected CHPL Product Number %s must be unique among all other certified products but one already exists with this value.";

    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil errorMessageUtil;
    private ChplNumberUniqueReviewer reviewer;

    @Before
    public void setup() {
        chplProductNumberUtil =  Mockito.mock(ChplProductNumberUtil.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        reviewer = new ChplNumberUniqueReviewer(chplProductNumberUtil, errorMessageUtil);
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
    public void review_invalidFormatOfChplProductNumberListingNotFound_noError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumber.notUnique"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CHPL_NUMBER_NOT_UNIQUE, i.getArgument(1), ""));

        Mockito.when(chplProductNumberUtil.getListing(ArgumentMatchers.eq("bad.format")))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("bad.format")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidFormatOfChplProductNumberListingFound_hasError() throws ParseException {
        String badChplProductNumber = "bad.format";

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumber.notUnique"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CHPL_NUMBER_NOT_UNIQUE, i.getArgument(1), ""));

        Mockito.when(chplProductNumberUtil.getListing(ArgumentMatchers.eq(badChplProductNumber)))
        .thenReturn(CertifiedProduct.builder()
                .id(1L)
                .chplProductNumber(badChplProductNumber)
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(badChplProductNumber)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CHPL_NUMBER_NOT_UNIQUE, badChplProductNumber, "")));
    }

    @Test
    public void review_badFormatOfChplProductNumberNoListingFound_noError() throws ParseException {
        String badChplProductNumber = "15.04.04.2526.WEBe.06.00.0.210102";

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumber.notUnique"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CHPL_NUMBER_NOT_UNIQUE, i.getArgument(1), ""));

        Mockito.when(chplProductNumberUtil.getListing(ArgumentMatchers.eq(badChplProductNumber)))
        .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(badChplProductNumber)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_goodFormatOfChplProductNumberListingFound_hasError() throws ParseException {
        String badChplProductNumber = "15.04.04.2526.WEBe.06.00.0.210102";

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumber.notUnique"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CHPL_NUMBER_NOT_UNIQUE, i.getArgument(1), ""));

        Mockito.when(chplProductNumberUtil.getListing(ArgumentMatchers.eq(badChplProductNumber)))
        .thenReturn(CertifiedProduct.builder()
                .id(1L)
                .chplProductNumber(badChplProductNumber)
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(badChplProductNumber)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CHPL_NUMBER_NOT_UNIQUE, badChplProductNumber, "")));
    }
}
