package gov.healthit.chpl.upload.listing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
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
    private static final String HEADER_SINGLE_COLUMN = "RECORD_STATUS__C";
    private static final String MULTIPLE_ROWS = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C\n"
                                                + "15.02.02.3007.A056.01.00.0.180214,New";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,New";

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandler handler;

    @Before
    public void setup() throws InvalidArgumentsException, JsonProcessingException,
        EntityRetrievalException, EntityCreationException, IOException, FileNotFoundException {

        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.emptyFile"))).thenReturn("Empty file message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.notCSV"))).thenReturn("Not CSV message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.emptyRows"))).thenReturn("Header only message");

        handler = new ListingUploadHandler(msgUtil);
    }

    @Test
    public void getHeadingRecordIndex_EmptyData_ReturnsCorrectValue() {
        int index = handler.getHeadingRecordIndex(new ArrayList<CSVRecord>());
        assertEquals(-1, index);
    }

    @Test
    public void getHeadingRecordIndex_NullData_ReturnsCorrectValue() {
        int index = handler.getHeadingRecordIndex(null);
        assertEquals(-1, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnly_ReturnsCorrectValue() throws IOException {
        List<CSVRecord> records = null;
        StringReader in = new StringReader(HEADER_COMMON_NAMES);
        CSVParser csvParser = CSVFormat.EXCEL.parse(in);
        records = csvParser.getRecords();

        int index = handler.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnlyAlternateNames_ReturnsCorrectValue()
            throws IOException {
        List<CSVRecord> records = null;
        StringReader in = new StringReader(HEADER_ALT_NAMES);
        CSVParser csvParser = CSVFormat.EXCEL.parse(in);
        records = csvParser.getRecords();

        int index = handler.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnlySingleColumn_ReturnsCorrectValue()
            throws IOException {
        List<CSVRecord> records = null;
        StringReader in = new StringReader(HEADER_SINGLE_COLUMN);
        CSVParser csvParser = CSVFormat.EXCEL.parse(in);
        records = csvParser.getRecords();

        int index = handler.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_MultiRowData_ReturnsCorrectValue()
            throws IOException {
        List<CSVRecord> records = null;
        StringReader in = new StringReader(MULTIPLE_ROWS);
        CSVParser csvParser = CSVFormat.EXCEL.parse(in);
        records = csvParser.getRecords();

        int index = handler.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecord_MultiRowData_ReturnsCorrectValue()
            throws IOException {
        List<CSVRecord> records = null;
        StringReader in = new StringReader(MULTIPLE_ROWS);
        CSVParser csvParser = CSVFormat.EXCEL.parse(in);
        records = csvParser.getRecords();
        assertEquals(2, records.size());

        CSVRecord heading = handler.getHeadingRecord(records);
        assertNotNull(heading);
        assertEquals("UNIQUE_CHPL_ID__C", heading.get(0));
    }

    @Test
    public void getChplProductNumber_MultiRowData_ReturnsCorrectValue()
            throws IOException {
        List<CSVRecord> records = null;
        StringReader in = new StringReader(HEADER_ALT_NAMES);
        CSVParser csvParser = CSVFormat.EXCEL.parse(in);
        records = csvParser.getRecords();
        assertEquals(2, records.size());

        CSVRecord heading = handler.parseChplProductNumber(headingRecord, listingRecord)
        assertNotNull(heading);
        assertEquals("UNIQUE_CHPL_ID__C", heading.get(0));
    }

    private List<CSVRecord> getRecordsFromString(String str) {
        List<CSVRecord> records = null;
        try {
            StringReader in = new StringReader(HEADER_ALT_NAMES);
            CSVParser csvParser = CSVFormat.EXCEL.parse(in);
            records = csvParser.getRecords();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        return records;
    }
}
