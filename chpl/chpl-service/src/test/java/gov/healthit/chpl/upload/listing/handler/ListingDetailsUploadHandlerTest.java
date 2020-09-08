package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingDetailsUploadHandlerTest {
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
    private ListingDetailsUploadHandler handler;

    @Before
    public void setup() throws InvalidArgumentsException, JsonProcessingException,
        EntityRetrievalException, EntityCreationException, IOException, FileNotFoundException {

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
        handler = new ListingDetailsUploadHandler(handlerUtil, msgUtil);
    }

    @Test
    public void getHeadingRecord_MultiRowData_ReturnsCorrectValue() {
        CSVRecord headingRecord = getRecordsFromString(HEADER_COMMON_NAMES).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = getRecordsFromString(LISTING_ROW);
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

    private List<CSVRecord> getRecordsFromString(String str) {
        List<CSVRecord> records = null;
        try {
            StringReader in = new StringReader(str);
            CSVParser csvParser = CSVFormat.EXCEL.parse(in);
            records = csvParser.getRecords();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        return records;
    }
}
