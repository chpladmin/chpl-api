package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestProcedureUploadHandlerTest {
    private static final String HEADER_ROW_ALL_TP_FIELDS = "CRITERIA_170_315_A_1__C,Test Procedure,Test procedure version";
    private static final String HEADER_ROW_TP_VERSION_ONLY = "CRITERIA_170_315_A_1__C,Test procedure version";

    private TestProcedureUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        FF4j ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CONFORMANCE_METHOD)))
            .thenReturn(false);
        handler = new TestProcedureUploadHandler(handlerUtil, ff4j);
    }

    @Test
    public void parseTestProcedure_NoTestProcedureColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(0, parsedTps.size());
    }

    @Test
    public void parseTestProcedure_TestProcedureAllColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TP_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,,");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(0, parsedTps.size());
    }

    @Test
    public void parseTestProcedure_TestProcedureVersionColumnNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_TP_VERSION_ONLY).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(0, parsedTps.size());
    }

    @Test
    public void parseTestProcedure_SingleTestProcedureAndBlankLine_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TP_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,ONC Test Procedure,2.0" + "\n" + ",,");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(1, parsedTps.size());
    }

    @Test
    public void parseTestProcedure_MultipleTestProceduresAllFieldsPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TP_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,ONC Test Procedure,2.0" + "\n" + ",TP 2,1.0");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(2, parsedTps.size());
        parsedTps.stream().forEach(tp -> {
            assertNull(tp.getId());
            assertNotNull(tp.getTestProcedure());
            assertNotNull(tp.getTestProcedureVersion());
            if (tp.getTestProcedure().getName().equals("ONC Test Procedure")) {
                assertEquals("2.0", tp.getTestProcedureVersion());
            } else if (tp.getTestProcedure().getName().equals("TP 2")) {
                assertEquals("1.0", tp.getTestProcedureVersion());
            } else {
                fail("No Test Procedure with name " + tp.getTestProcedure().getName() + " should have been found.");
            }
        });
    }

    @Test
    public void parseTestProcedures_MultipleTestProceduresVersionOnlyPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TP_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,,1.0\n,,v5");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(2, parsedTps.size());
        parsedTps.stream().forEach(tp -> {
            assertNull(tp.getId());
            assertNotNull(tp.getTestProcedure());
            assertEquals("", tp.getTestProcedure().getName());
            assertTrue(tp.getTestProcedureVersion().equals("1.0") || tp.getTestProcedureVersion().equals("v5"));
        });
    }

    @Test
    public void parseTestProcedure_SingleTestProcedureSomeFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_TP_VERSION_ONLY).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,v1.6");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(1, parsedTps.size());
        CertificationResultTestProcedure tp = parsedTps.get(0);
        assertNull(tp.getTestProcedure());
        assertEquals("v1.6", tp.getTestProcedureVersion());
    }

    @Test
    public void parseTestProcedure_SingleTestProcedureUnexpectedHeaderOrder_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test procedure version,Test Procedure").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1.0,ONC Test Method");
        assertNotNull(certResultRecords);

        List<CertificationResultTestProcedure> parsedTps = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTps);
        assertEquals(1, parsedTps.size());
        CertificationResultTestProcedure tp = parsedTps.get(0);
        assertNotNull(tp.getTestProcedure());
        assertEquals("ONC Test Method", tp.getTestProcedure().getName());
        assertNull(tp.getTestProcedure().getId());
        assertEquals("1.0", tp.getTestProcedureVersion());
    }
}
