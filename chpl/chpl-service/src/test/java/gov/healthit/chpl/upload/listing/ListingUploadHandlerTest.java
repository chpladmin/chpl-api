package gov.healthit.chpl.upload.listing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingUploadHandlerTest {
    private static final String HEADER_COMMON_NAMES = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,VENDOR__C";
    private static final String HEADER_ALT_NAMES = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C";
    private static final String HEADER_WITH_SPACES = " UNIQUE_CHPL_ID__C , RECORD_STATUS__C ";
    private static final String HEADER_SINGLE_COLUMN = "RECORD_STATUS__C";
    private static final String HEADER_DUPLICATE = "UNIQUE_CHPL_ID__C,UNIQUE_CHPL_ID__C,RECORD_STATUS__C";
    private static final String MULTIPLE_ROWS = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C\n"
                                                + "15.02.02.3007.A056.01.00.0.180214,New";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,New";

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;

    @Before
    public void setup() throws InvalidArgumentsException, JsonProcessingException,
        EntityRetrievalException, EntityCreationException, IOException, FileNotFoundException {

        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.emptyFile"))).thenReturn("Empty file message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.notCSV"))).thenReturn("Not CSV message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.emptyRows"))).thenReturn("Header only message");

        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
    }

    @Test
    public void getHeadingRecordIndex_EmptyData_ReturnsCorrectValue() {
        int index = handlerUtil.getHeadingRecordIndex(new ArrayList<CSVRecord>());
        assertEquals(-1, index);
    }

    @Test
    public void getHeadingRecordIndex_NullData_ReturnsCorrectValue() {
        int index = handlerUtil.getHeadingRecordIndex(null);
        assertEquals(-1, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnly_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingWithSpaces_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_WITH_SPACES);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnlyAlternateNames_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_ALT_NAMES);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnlySingleColumn_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_SINGLE_COLUMN);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_MultiRowData_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(MULTIPLE_ROWS);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecord_MultiRowData_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(MULTIPLE_ROWS);
        assertEquals(2, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        assertEquals("UNIQUE_CHPL_ID__C", heading.get(0));
    }

    @Test(expected = ValidationException.class)
    public void getChplProductNumber_NoChplProductNumberHeading_ThrowsException()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_SINGLE_COLUMN);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        handlerUtil.parseRequiredSingleValueField(Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
    }

    @Test
    public void getChplProductNumber_EmptyData_ReturnsNullValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        String chplProductNumber = handlerUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecords.get(0), new ArrayList<CSVRecord>());
        assertNull(chplProductNumber);
    }

    @Test
    public void getChplProductNumber_EmptyStringData_ReturnsEmptyValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(",,");
        String chplProductNumber = handlerUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_MultiRowData_ReturnsCorrectValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        String chplProductNumber = handlerUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("15.02.02.3007.A056.01.00.0.180214", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_ExtraSpacesData_ReturnsTrimmedValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(" extra spaces ,new,dev");
        String chplProductNumber = handlerUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("extra spaces", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_ExtraSpacesHeaderAndData_ReturnsTrimmedValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_WITH_SPACES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(" extra spaces ,new");
        String chplProductNumber = handlerUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("extra spaces", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_DuplicateHeaders_ReturnsFirstValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_DUPLICATE);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("first,second,status");
        String chplProductNumber = handlerUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("first", chplProductNumber);
    }
}
