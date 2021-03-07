package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class ChplNumberFormatReviewerTest {
    private static final String CHPL_PRODUCT_NUMBER_MISSING = "A CHPL Product Number is required but not found.";
    private static final String BAD_CHPL_PRODUCT_NUMBER_FORMAT = "The CHPL Product Number is an invalid format. It requires %s parts separated by a '.'.";
    private static final String BAD_EDITION_CODE = "The Edition code is required and must be %s characters in length containing only the characters 0-9.";
    private static final String BAD_ATL_CODE = "The ONC-ATL code is required and must be %s characters in length containing only the characters 0-9.";
    private static final String BAD_ACB_CODE = "The ONC-ACB code is required and must be %s characters in length containing only the characters 0-9.";
    private static final String BAD_DEVELOPER_CODE = "The developer code is required and must be %s characters in length containing only the characters 0-9.";
    private static final String BAD_PRODUCT_CODE = "The product code is required and must be %s characters in length containing only the characters A-Z, a-z, 0-9, and _.";
    private static final String BAD_VERSION_CODE = "The version code is required and must be %s characters in length containing only the characters A-Z, a-z, 0-9, and _.";
    private static final String BAD_ICS_CODE = "The ICS code is required and must be %s characters in length with a value between 00-99. If you have exceeded the maximum inheritance level of 99, please contact the CHPL team for further assistance.";
    private static final String BAD_ADDITIONAL_SOFTWARE_CODE = "The additional software code is required and must be %s character in length containing only the characters 0 or 1.";
    private static final String BAD_CERT_DATE_CODE = "The certified date code is required and must be %s characters in length containing only the characters 0-9.";

    private ErrorMessageUtil errorMessageUtil;
    private ChplNumberFormatReviewer reviewer;

    @Before
    public void setup() {
        ValidationUtils validationUtils = new ValidationUtils();
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new ChplNumberFormatReviewer(validationUtils, errorMessageUtil);
    }

    @Test
    public void review_chplProductNumberValid_noError() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEeB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_chplProductNumberNull_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumberMissing")))
            .thenReturn(CHPL_PRODUCT_NUMBER_MISSING);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CHPL_PRODUCT_NUMBER_MISSING));
    }

    @Test
    public void review_chplProductNumberEmpty_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumberMissing")))
            .thenReturn(CHPL_PRODUCT_NUMBER_MISSING);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(CHPL_PRODUCT_NUMBER_MISSING));
    }

    @Test
    public void review_chplProductNumberNotEnoughSections_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumberInvalidFormat"), ArgumentMatchers.any()))
        .thenAnswer(i -> String.format(BAD_CHPL_PRODUCT_NUMBER_FORMAT, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("bad.format")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CHPL_PRODUCT_NUMBER_FORMAT, "9", "")));
    }

    @Test
    public void review_chplProductNumberNoSections_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.chplProductNumberInvalidFormat"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_CHPL_PRODUCT_NUMBER_FORMAT, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("badformat")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CHPL_PRODUCT_NUMBER_FORMAT, "9", "")));
    }

    @Test
    public void review_editionCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badEditionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_EDITION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("2015.04.04.2526.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_EDITION_CODE, "2", "")));
    }

    @Test
    public void review_editionCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badEditionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_EDITION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("5.04.04.2526.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_EDITION_CODE, "2", "")));
    }

    @Test
    public void review_editionCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badEditionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_EDITION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("1r.04.04.2526.WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_EDITION_CODE, "2", "")));
    }

    @Test
    public void review_editionCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badEditionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_EDITION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(".04.04.2526.WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_EDITION_CODE, "2", "")));
    }

    @Test
    public void review_atlCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAtlCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ATL_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.100.04.2526.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ATL_CODE, "2", "")));
    }

    @Test
    public void review_atlCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAtlCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ATL_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.4.04.2526.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ATL_CODE, "2", "")));
    }

    @Test
    public void review_atlCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAtlCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ATL_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.r4.04.2526.WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ATL_CODE, "2", "")));
    }

    @Test
    public void review_atlCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAtlCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ATL_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15..04.2526.WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ATL_CODE, "2", "")));
    }

    @Test
    public void review_acbCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAcbCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ACB_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.040.2526.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ACB_CODE, "2", "")));
    }

    @Test
    public void review_acbCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAcbCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ACB_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.4.2526.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ACB_CODE, "2", "")));
    }

    @Test
    public void review_acbCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAcbCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ACB_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04._4.2526.WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ACB_CODE, "2", "")));
    }

    @Test
    public void review_acbCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAcbCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ACB_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04..2526.WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ACB_CODE, "2", "")));
    }

    @Test
    public void review_developerCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badDeveloperCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_DEVELOPER_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.25626.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_DEVELOPER_CODE, "4", "")));
    }

    @Test
    public void review_developerCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badDeveloperCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_DEVELOPER_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.226.WEBe.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_DEVELOPER_CODE, "4", "")));
    }

    @Test
    public void review_developerCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badDeveloperCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_DEVELOPER_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.65_6.WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_DEVELOPER_CODE, "4", "")));
    }

    @Test
    public void review_developerCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badDeveloperCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_DEVELOPER_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04..WErB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_DEVELOPER_CODE, "4", "")));
    }

    @Test
    public void review_productCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badProductCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_PRODUCT_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe7.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_PRODUCT_CODE, "4", "")));
    }

    @Test
    public void review_productCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badProductCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_PRODUCT_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEB.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_PRODUCT_CODE, "4", "")));
    }

    @Test
    public void review_productCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badProductCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_PRODUCT_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WE!B.06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_PRODUCT_CODE, "4", "")));
    }

    @Test
    public void review_productCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badProductCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_PRODUCT_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526..06.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_PRODUCT_CODE, "4", "")));
    }

    @Test
    public void review_versionCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badVersionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_VERSION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.40.2526.WEBe.067.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_VERSION_CODE, "2", "")));
    }

    @Test
    public void review_versionCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badVersionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_VERSION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.1.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_VERSION_CODE, "2", "")));
    }

    @Test
    public void review_versionCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badVersionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_VERSION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.7!.00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_VERSION_CODE, "2", "")));
    }

    @Test
    public void review_versionCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badVersionCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_VERSION_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB..00.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_VERSION_CODE, "2", "")));
    }

    @Test
    public void review_icsCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badIcsCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ICS_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.40.2526.WEBe.07.999.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ICS_CODE, "2", "")));
    }

    @Test
    public void review_icsCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badIcsCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ICS_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.01.0.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ICS_CODE, "2", "")));
    }

    @Test
    public void review_icsCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badIcsCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ICS_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.73.rt.1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ICS_CODE, "2", "")));
    }

    @Test
    public void review_icsCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badIcsCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ICS_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.6T..1.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ICS_CODE, "2", "")));
    }

    @Test
    public void review_additionalSoftwareCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAdditionalSoftwareCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ADDITIONAL_SOFTWARE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.40.2526.WEBe.07.99.16.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ADDITIONAL_SOFTWARE_CODE, "1", "")));
    }

    @Test
    public void review_additionalSoftwareCodeInvalidNumber_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAdditionalSoftwareCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ADDITIONAL_SOFTWARE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.73.00.2.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ADDITIONAL_SOFTWARE_CODE, "1", "")));
    }

    @Test
    public void review_additionalSoftwareCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAdditionalSoftwareCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ADDITIONAL_SOFTWARE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.73.00.Y.210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ADDITIONAL_SOFTWARE_CODE, "1", "")));
    }

    @Test
    public void review_additionalSoftwareCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badAdditionalSoftwareCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_ADDITIONAL_SOFTWARE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.6T.00..210101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_ADDITIONAL_SOFTWARE_CODE, "1", "")));
    }

    @Test
    public void review_certificationDateCodeTooLong_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badCertifiedDateCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_CERT_DATE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2562.WEBe.06.00.1.2101015")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CERT_DATE_CODE, "6", "")));
    }

    @Test
    public void review_certificationDateCodeTooShort_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badCertifiedDateCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_CERT_DATE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.2101")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CERT_DATE_CODE, "6", "")));
    }

    @Test
    public void review_certificationDateCodeInvalidCharacter_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badCertifiedDateCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_CERT_DATE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.6566.WErB.06.00.1.21JA01")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CERT_DATE_CODE, "6", "")));
    }

    @Test
    public void review_certificationDateCodeMissing_hasError() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badCertifiedDateCodeChars"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(BAD_CERT_DATE_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.00.1. ")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        System.out.println(listing.getErrorMessages().iterator().next());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CERT_DATE_CODE, "6", "")));
    }
}
