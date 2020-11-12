package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class IcsUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214,New";
    private static final String LISTING_ROW_SUBELEMENT_BEGIN = "15.02.02.3007.A056.01.00.0.180214,Subelement";

    private IcsUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new IcsUploadHandler(handlerUtil);
    }

    @Test
    public void parseIcs_NoIcsColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNull(parsedIcs);
    }

    @Test
    public void parseIcs_IcsColumnsNoData_ReturnsListWithEmptyItems() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertFalse(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(0, parsedIcs.getParents().size());
        assertNotNull(parsedIcs.getChildren());
        assertEquals(0, parsedIcs.getChildren().size());
    }

    @Test
    public void parseIcs_MultipleIcsAllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",1,CHP-12345\n"
                + LISTING_ROW_SUBELEMENT_BEGIN + ",,CHP-23456");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertEquals(true, parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(2, parsedIcs.getParents().size());
        parsedIcs.getParents().stream().forEach(icsParent -> {
            assertNotNull(icsParent.getChplProductNumber());
            assertTrue(icsParent.getChplProductNumber().equals("CHP-12345")
                    || icsParent.getChplProductNumber().equals("CHP-23456"));
        });
    }

    @Test
    public void parseIcs_MultipleIcsSomeFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",0,\n"
                + LISTING_ROW_SUBELEMENT_BEGIN + ",,");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertEquals(false, parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(0, parsedIcs.getParents().size());
    }

    @Test
    public void parseIcs_SingleIcsAllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",1,CHP-12345");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertTrue(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(1, parsedIcs.getParents().size());
        assertEquals("CHP-12345", parsedIcs.getParents().get(0).getChplProductNumber());
    }

    @Test
    public void parseIcs_SingleIcsUnexpectedHeaderOrder_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS Source,ICS").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",CHP-12345,1");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertTrue(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(1, parsedIcs.getParents().size());
        assertEquals("CHP-12345", parsedIcs.getParents().get(0).getChplProductNumber());
    }

    @Test
    public void parseIcs_SingleNoSourceColumn_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",0");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertFalse(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(0, parsedIcs.getParents().size());
    }

    @Test
    public void parseIcs_SingleNoIcsColumn_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",CHP-12345");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertNull(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(1, parsedIcs.getParents().size());
    }
}
