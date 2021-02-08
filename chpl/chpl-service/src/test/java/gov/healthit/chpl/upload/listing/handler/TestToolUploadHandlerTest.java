package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestToolUploadHandlerTest {
    private static final String HEADER_ROW_ALL_TT_FIELDS = "CRITERIA_170_315_A_1__C,Test tool name,Test tool version";
    private static final String HEADER_ROW_TT_VERSION_ONLY = "CRITERIA_170_315_A_1__C,Test tool version";

    private TestToolUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new TestToolUploadHandler(handlerUtil);
    }

    @Test
    public void parseTestTool_NoTestToolColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(0, parsedTestTools.size());
    }

    @Test
    public void parseTestTool_TestToolAllColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TT_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,,");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(0, parsedTestTools.size());
    }

    @Test
    public void parseTestTool_TestToolVersionColumnNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_TT_VERSION_ONLY).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(0, parsedTestTools.size());
    }

    @Test
    public void parseTestTool_SingleTestToolsAndBlankLine_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TT_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,Test Tool 1,2.0" + "\n" + ",,");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(1, parsedTestTools.size());
    }

    @Test
    public void parseTestTool_MultipleTestToolsAllFieldsPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TT_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,Test Tool 1,2.0" + "\n" + ",Test Tool 2,1.0");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(2, parsedTestTools.size());
        parsedTestTools.stream().forEach(tt -> {
            assertNull(tt.getTestToolId());
            assertNotNull(tt.getTestToolName());
            assertNotNull(tt.getTestToolVersion());
            if (tt.getTestToolName().equals("Test Tool 1")) {
                assertEquals("2.0", tt.getTestToolVersion());
            } else if (tt.getTestToolName().equals("Test Tool 2")) {
                assertEquals("1.0", tt.getTestToolVersion());
            } else {
                fail("No Test Tool with name " + tt.getTestToolName() + " should have been found.");
            }
        });
    }

    @Test
    public void parseTestTools_MultipleTestToolsVersionOnlyPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TT_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,,1.0\n,,v5");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(2, parsedTestTools.size());
        parsedTestTools.stream().forEach(tt -> {
            assertNull(tt.getTestToolId());
            assertEquals("", tt.getTestToolName());
            assertTrue(tt.getTestToolVersion().equals("1.0") || tt.getTestToolVersion().equals("v5"));
        });
    }

    @Test
    public void parseTestTool_SingleTestToolVersionOnlyPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_TT_VERSION_ONLY).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,v1.6");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(1, parsedTestTools.size());
        CertificationResultTestTool tt = parsedTestTools.get(0);
        assertNull(tt.getTestToolName());
        assertNull(tt.getTestToolId());
        assertEquals("v1.6", tt.getTestToolVersion());
    }

    @Test
    public void parseTestTool_SingleTestToolUnexpectedHeaderOrder_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool version,Test tool name").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1.0,ONC Test Tool");
        assertNotNull(certResultRecords);

        List<CertificationResultTestTool> parsedTestTools = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestTools);
        assertEquals(1, parsedTestTools.size());
        CertificationResultTestTool tt = parsedTestTools.get(0);
        assertEquals("ONC Test Tool", tt.getTestToolName());
        assertEquals("1.0", tt.getTestToolVersion());
    }
}
