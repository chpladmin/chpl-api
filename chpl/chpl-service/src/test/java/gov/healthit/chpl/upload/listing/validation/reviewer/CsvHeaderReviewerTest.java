package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CsvHeaderReviewerTest {
    private static final String CSV_INVALID_HEADER = "The heading '%s' was found in the upload file but is not recognized.";
    private static final String CSV_DUPLICATE_HEADER = "The heading '%s' appears to be a duplicate in the file.";
    private static final String CSV_DUPLICATE_CRITERIA_HEADER = "The heading '%s' appears to be a duplicate for certification result %s.";

    private ErrorMessageUtil errorMessageUtil;
    private CSVHeaderReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.upload.unrecognizedHeading"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CSV_INVALID_HEADER, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.upload.duplicateHeading"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CSV_DUPLICATE_HEADER, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.upload.duplicateCriteriaHeading"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CSV_DUPLICATE_CRITERIA_HEADER, i.getArgument(1), i.getArgument(2)));

        ListingUploadHandlerUtil uploadHandlerUtil = new ListingUploadHandlerUtil(errorMessageUtil);
        reviewer = new CSVHeaderReviewer(uploadHandlerUtil, errorMessageUtil);
    }

    @Test
    public void review_nullHeader_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(null)
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_emptyHeader_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(new ArrayList<CSVRecord>())
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_blankHeaderValue_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_invalidHeadingInMiddle_hasWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,UNKNOWNVALUE,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
    }

    @Test
    public void review_invalidHeadingInBeginning_hasWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNKNOWNVALUE,UNIQUE_CHPL_ID__C,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
    }

    @Test
    public void review_invalidHeadingAtEnd_hasWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,UNKNOWNVALUE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
    }

    @Test
    public void review_multipleIinvalidHeadings_hasWarnings() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,UNKNOWNVALUE__C,VENDOR__C,UNKNOWNVALUE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsApart_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsAdjacent_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "PRODUCT__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsAdjacentAtBeginning_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "UNIQUE_CHPL_ID__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsAdjacentAtEnd_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR_PHONE", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsHaveDifferentStringsMappedToSameValue_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,PRODUCT,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "PRODUCT", "")));
    }

    @Test
    public void review_twoDuplicateListingLevelHeading_HeadingsHaveDifferentStringsMappedToSameValue_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,DEVELOPER,VENDOR__C,PRODUCT__C,PRODUCT,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "PRODUCT", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR__C", "")));
    }

    @Test
    public void review_multipleDuplicateListingLevelHeadings_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR_WEBSITE,VENDOR__C,PRODUCT__C,UNIQUE_CHPL_ID__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR_PHONE", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR_WEBSITE", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "UNIQUE_CHPL_ID__C", "")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_SingleCriteriaAndHeadingsAdjacent_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,GAP,Test Data,Test Procedure"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_SingleCriteriaAndHeadingsApart_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_MultipleCriteriaAndHeadingsApart_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_HeadingsHaveDifferentStringsMappedToSameValue_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,Standard Tested Against,Optional Standard"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Optional Standard", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_twoDuplicateCertResultLevelHeadings_MultipleCriteria_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,Privacy and Security Framework,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Privacy and Security Framework", "CRITERIA_170_315_A_9__C")));
    }

    @Test
    public void review_multipleDuplicateCertResultLevelHeadings_MultipleCriteriaWithSameDuplicatedColumn_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,GAP,Privacy and Security Framework,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_9__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Privacy and Security Framework", "CRITERIA_170_315_A_9__C")));
    }

    @Test
    public void review_oneDuplicateCriterion_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,Test Data,Test Procedure,CRITERIA_170_315_A_8__C,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_twoDuplicateCriteria_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,Test Data,Test Procedure,CRITERIA_170_315_A_8__C,GAP,Additional Software,Privacy and Security Framework,CRITERIA_170_315_A_1__C,CRITERIA_170_315_A_1__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "CRITERIA_170_315_A_1__C")));
    }

    @Test
    public void review_multipleDuplicateListingAndCertResultLevelHeadings_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,GAP,Privacy and Security Framework,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(4, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR__C", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_9__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Privacy and Security Framework", "CRITERIA_170_315_A_9__C")));
    }
}
