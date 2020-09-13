package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingDetailsUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214,New";;

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;
    private ListingDetailsUploadHandler handler;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.emptyFile"))).thenReturn("Empty file message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.notCSV"))).thenReturn("Not CSV message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.emptyRows"))).thenReturn("Header only message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.requiredHeadingNotFound"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The required heading %s was not found.", i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.invalidBoolean"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The value %s could not be converted to a yes/no field..", i.getArgument(1), ""));


        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new ListingDetailsUploadHandler(Mockito.mock(DeveloperDetailsUploadHandler.class),
                Mockito.mock(TargetedUsersUploadHandler.class),
                Mockito.mock(AccessibilityStandardsUploadHandler.class),
                Mockito.mock(QmsUploadHandler.class),
                null, null, null, null, null,
                handlerUtil, msgUtil);
    }

    @Test
    public void buildListing_GoodData_ReturnsCorrectChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getChplProductNumber());
            assertEquals("15.02.02.3007.A056.01.00.0.180214", listing.getChplProductNumber());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void buildListing_ChplProductNumberEmpty_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(",New");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getChplProductNumber());
            assertEquals("", listing.getChplProductNumber());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void buildListing_ChplProductNumberWhitespace_TrimsResult() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("  ,New");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getChplProductNumber());
            assertEquals("", listing.getChplProductNumber());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = ValidationException.class)
    public void buildListing_ChplProductNumberColumnMissing_ThrowsException() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("RECORD_STATUS__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("New");
        assertNotNull(listingRecords);

        handler.parseAsListing(headingRecord, listingRecords);
    }

    @Test
    public void buildListing_BooleanValue0_ReturnsFalseAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",0");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getAccessibilityCertified());
            assertEquals(Boolean.FALSE, listing.getAccessibilityCertified());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void buildListing_BooleanValue1_ReturnsTrueAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",1");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getAccessibilityCertified());
            assertEquals(Boolean.TRUE, listing.getAccessibilityCertified());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void buildListing_BooleanValueNo_ReturnsTrueAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN+",No");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getAccessibilityCertified());
            assertEquals(Boolean.FALSE, listing.getAccessibilityCertified());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void buildListing_BooleanValueYes_ReturnsTrueAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Yes");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getAccessibilityCertified());
            assertEquals(Boolean.TRUE, listing.getAccessibilityCertified());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = ValidationException.class)
    public void buildListing_BooleanValueBad_ThrowsException() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",JUNK");
        assertNotNull(listingRecords);

        handler.parseAsListing(headingRecord, listingRecords);
    }

    @Test
    public void buildListing_CertificationDateGood_ParsesDateValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",20200909");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNotNull(listing.getCertificationDate());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void buildListing_CertificationDateEmpty_ParsesNullValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        try {
            CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
            assertNotNull(listing);
            assertNull(listing.getCertificationDate());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = ValidationException.class)
    public void buildListing_CertificationDateValueBad_ThrowsException() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",BADDATE");
        assertNotNull(listingRecords);

        handler.parseAsListing(headingRecord, listingRecords);
    }

    //TODO: add tests for edition, product, version, acb, atl, k1 url
}
