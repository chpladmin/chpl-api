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
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ConformanceMethodUploadHandlerTest {
    private static final String HEADER_ROW_ALL_CM_FIELDS = "CRITERIA_170_315_A_1__C,Conformance Method,Conformance Method Version";
    private static final String HEADER_ROW_CM_VERSION_ONLY = "CRITERIA_170_315_A_1__C,Conformance Method Version";

    private ConformanceMethodUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        FF4j ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CONFORMANCE_METHOD)))
            .thenReturn(true);
        handler = new ConformanceMethodUploadHandler(ff4j, handlerUtil);
    }

    @Test
    public void parseConformanceMethod_NoConformanceMethodColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(0, parsedCms.size());
    }

    @Test
    public void parseConformanceMethod_ConformanceMethodAllColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_CM_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,,");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(0, parsedCms.size());
    }

    @Test
    public void parseConformanceMethod_ConformanceMethodVersionColumnNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_CM_VERSION_ONLY).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(0, parsedCms.size());
    }

    @Test
    public void parseConformanceMethod_SingleConformanceMethodAndBlankLine_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_CM_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,ONC Conformance Method,2.0" + "\n" + ",,");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(1, parsedCms.size());
    }

    @Test
    public void parseConformanceMethod_MultipleConformanceMethodsAllFieldsPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_CM_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,ONC Conformance Method,2.0" + "\n" + ",CM 2,1.0");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(2, parsedCms.size());
        parsedCms.stream().forEach(cm -> {
            assertNull(cm.getId());
            assertNotNull(cm.getConformanceMethod());
            assertNotNull(cm.getConformanceMethodVersion());
            if (cm.getConformanceMethod().getName().equals("ONC Conformance Method")) {
                assertEquals("2.0", cm.getConformanceMethodVersion());
            } else if (cm.getConformanceMethod().getName().equals("CM 2")) {
                assertEquals("1.0", cm.getConformanceMethodVersion());
            } else {
                fail("No Conformance Method with name " + cm.getConformanceMethod().getName() + " should have been found.");
            }
        });
    }

    @Test
    public void parseConformanceMethods_MultipleConformanceMethodsVersionOnlyPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_CM_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,,1.0\n,,v5");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(2, parsedCms.size());
        parsedCms.stream().forEach(cm -> {
            assertNull(cm.getId());
            assertNotNull(cm.getConformanceMethod());
            assertEquals("", cm.getConformanceMethod().getName());
            assertTrue(cm.getConformanceMethodVersion().equals("1.0") || cm.getConformanceMethodVersion().equals("v5"));
        });
    }

    @Test
    public void parseConformanceMethod_SingleConformanceMethodSomeFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_CM_VERSION_ONLY).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,v1.6");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(1, parsedCms.size());
        CertificationResultConformanceMethod cm = parsedCms.get(0);
        assertNull(cm.getConformanceMethod());
        assertEquals("v1.6", cm.getConformanceMethodVersion());
    }

    @Test
    public void parseConformanceMethod_SingleConformanceMethodUnexpectedHeaderOrder_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Conformance Method Version,Conformance Method").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1.0,ONC Test Method");
        assertNotNull(certResultRecords);

        List<CertificationResultConformanceMethod> parsedCms = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedCms);
        assertEquals(1, parsedCms.size());
        CertificationResultConformanceMethod cm = parsedCms.get(0);
        assertNotNull(cm.getConformanceMethod());
        assertEquals("ONC Test Method", cm.getConformanceMethod().getName());
        assertNull(cm.getConformanceMethod().getId());
        assertEquals("1.0", cm.getConformanceMethodVersion());
    }
}
