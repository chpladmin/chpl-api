package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AdditionalSoftwareUploadHandlerTest {
    private static final String HEADER_ROW_ALL_AS_FIELDS = "CRITERIA_170_315_A_1__C,Additional Software,CP Source,"
            + "CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping";

    private AdditionalSoftwareUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new AdditionalSoftwareUploadHandler(handlerUtil);
    }

    @Test
    public void parseAdditionalSoftware_NoAdditionalSoftwareColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(0, parsedAdditionalSoftware.size());
    }

    @Test
    public void parseAdditionalSoftware_AdditionalSoftwareAllColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_AS_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,,,,,,");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(0, parsedAdditionalSoftware.size());
    }

    @Test
    public void parseAdditionalSoftware_AdditionalSoftwareListingAndGroupColumnsOnly_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Additional Software,CP Source,CP Source Grouping").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1,CHP-12345,a");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(1, parsedAdditionalSoftware.size());
        CertificationResultAdditionalSoftware as = parsedAdditionalSoftware.get(0);
        assertEquals("CHP-12345", as.getCertifiedProductNumber());
        assertEquals("a", as.getGrouping());
        assertNull(as.getName());
        assertNull(as.getVersion());
    }

    @Test
    public void parseAdditionalSoftware_ListingAndGroupColumnsOnly_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,CP Source,CP Source Grouping").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,CHP-12345,a");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(1, parsedAdditionalSoftware.size());
        CertificationResultAdditionalSoftware as = parsedAdditionalSoftware.get(0);
        assertEquals("CHP-12345", as.getCertifiedProductNumber());
        assertEquals("a", as.getGrouping());
        assertNull(as.getName());
        assertNull(as.getVersion());
    }

    @Test
    public void parseAdditionalSoftware_AdditionalSoftwareNonCpColumns_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Additional Software,Non CP Source,Non CP Source Version,Non CP Source Grouping").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1,Windows,10,b");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(1, parsedAdditionalSoftware.size());
        CertificationResultAdditionalSoftware as = parsedAdditionalSoftware.get(0);
        assertNull(as.getCertifiedProductNumber());
        assertEquals("b", as.getGrouping());
        assertEquals("Windows", as.getName());
        assertEquals("10", as.getVersion());
    }

    @Test
    public void parseAdditionalSoftware_NonCpColumns_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Non CP Source,Non CP Source Version,Non CP Source Grouping").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Windows,10,b");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(1, parsedAdditionalSoftware.size());
        CertificationResultAdditionalSoftware as = parsedAdditionalSoftware.get(0);
        assertNull(as.getCertifiedProductNumber());
        assertEquals("b", as.getGrouping());
        assertEquals("Windows", as.getName());
        assertEquals("10", as.getVersion());
    }

    @Test
    public void parseAdditionalSoftware_AdditionalSoftwareBothGroupsWithData_ParsesCorrectGroup() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_AS_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1,,a,Windows,10,b");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(2, parsedAdditionalSoftware.size());
        parsedAdditionalSoftware.stream().forEach(as -> {
            if (as.getGrouping().equals("a")) {
                assertEquals("", as.getCertifiedProductNumber());
                assertNull(as.getName());
                assertNull(as.getVersion());
            } else if (as.getGrouping().equals("b")) {
                assertNull(as.getCertifiedProductNumber());
                assertEquals("Windows", as.getName());
                assertEquals("10", as.getVersion());
            } else {
                fail("No grouping with value " + as.getGrouping() + " was expected.");
            }
        });
    }

    @Test
    public void parseAdditionalSoftware_AdditionalSoftwareAllFieldsWithData_ParsesCorrectGroup() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_AS_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1,CHP-12345,a,Windows,10,b");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(2, parsedAdditionalSoftware.size());
        parsedAdditionalSoftware.stream().forEach(as -> {
            if (as.getGrouping().equals("a")) {
                assertEquals("CHP-12345", as.getCertifiedProductNumber());
                assertNull(as.getName());
                assertNull(as.getVersion());
            } else if (as.getGrouping().equals("b")) {
                assertNull(as.getCertifiedProductNumber());
                assertEquals("Windows", as.getName());
                assertEquals("10", as.getVersion());
            } else {
                fail("No grouping with value " + as.getGrouping() + " was expected.");
            }
        });
    }

    @Test
    public void parseAdditionalSoftware_SingleAdditionalSoftwareAndBlankLine_ParsesCorrectGroup() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_AS_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,1,CHP-12345,a,,,\n,,,,,,");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(1, parsedAdditionalSoftware.size());
    }

    @Test
    public void parseAdditionalSoftware_MultipleAdditionalSoftwaresAllFieldsPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_AS_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,1,CHP-1234,a,,," + "\n" + ",,,,Windows,10,b" + "\n" + ",,,,Windows,9,b");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(3, parsedAdditionalSoftware.size());
        parsedAdditionalSoftware.stream().forEach(as -> {
            assertNull(as.getId());
            assertNotNull(as.getGrouping());
            if (as.getCertifiedProductNumber() != null && as.getCertifiedProductNumber().equals("CHP-1234")) {
                assertEquals("a", as.getGrouping());
                assertNull(as.getName());
                assertNull(as.getVersion());
            } else if (as.getName() != null && as.getName().equals("Windows") && as.getVersion().equals("10")) {
                assertEquals("b", as.getGrouping());
                assertNull(as.getCertifiedProductNumber());
            } else if (as.getName() != null && as.getName().equals("Windows") && as.getVersion().equals("9")) {
                assertEquals("b", as.getGrouping());
                assertNull(as.getCertifiedProductNumber());
            } else {
                fail("No additional software with [chpl product number '" + as.getCertifiedProductNumber() + "'] "
                        + "or [name '" + as.getName() + "' and version '" + as.getVersion() + "'] should have been found.");
            }
        });
    }

    @Test
    public void parseAdditionalSoftware_SingleAdditionalSoftwareUnexpectedHeaderOrder_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Non CP Source Version,CP Source,Non CP Source,CP Source Grouping,"
                + "Non CP Source Grouping,Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,10,,Windows,,a,1");
        assertNotNull(certResultRecords);

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedAdditionalSoftware);
        assertEquals(1, parsedAdditionalSoftware.size());
        CertificationResultAdditionalSoftware as = parsedAdditionalSoftware.get(0);
        assertNull(as.getCertifiedProductNumber());
        assertEquals("a", as.getGrouping());
        assertEquals("Windows", as.getName());
        assertEquals("10", as.getVersion());
    }
}
