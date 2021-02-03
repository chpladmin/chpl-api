package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AccessibilityStandardsUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C,Accessibility Standard";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,170.204(a)(1)";
    private static final String LISTING_ROWS = "15.02.02.3007.A056.01.00.0.180214,170.204(a)(1)\n"
            + "15.02.02.3007.A056.01.00.0.180214,IEE 802.11";

    private AccessibilityStandardsUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new AccessibilityStandardsUploadHandler(handlerUtil);
    }

    @Test
    public void parseStandards_StandardsColumnNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "15.02.02.3007.A056.01.00.0.180214,");
        assertNotNull(listingRecords);

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(0, foundStandards.size());
    }

    @Test
    public void parseStandards_NoColumnNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "15.02.02.3007.A056.01.00.0.180214");
        assertNotNull(listingRecords);

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(0, foundStandards.size());
    }

    @Test
    public void parseStandards_MultipleStandards_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROWS);
        assertNotNull(listingRecords);

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(2, foundStandards.size());
        foundStandards.stream().forEach(std -> {
            assertNull(std.getId());
            assertNull(std.getAccessibilityStandardId());
            assertNotNull(std.getAccessibilityStandardName());
        });
    }

    @Test
    public void parseStandards_SingleStandard_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        assertNotNull(listingRecords);

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(1, foundStandards.size());
        foundStandards.stream().forEach(std -> {
            assertNull(std.getId());
            assertNull(std.getAccessibilityStandardId());
            assertNotNull(std.getAccessibilityStandardName());
            assertEquals("170.204(a)(1)", std.getAccessibilityStandardName());
        });
    }

    @Test
    public void parseStandards_StandardWithWhitespace_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "15.02.02.3007.A056.01.00.0.180214, 170.204(a)(1) ");
        assertNotNull(listingRecords);

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(1, foundStandards.size());
        foundStandards.stream().forEach(std -> {
            assertNull(std.getId());
            assertNull(std.getAccessibilityStandardId());
            assertNotNull(std.getAccessibilityStandardName());
            assertEquals("170.204(a)(1)", std.getAccessibilityStandardName());
        });
    }
}
