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

import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestDataUploadHandlerTest {
    private static final String HEADER_ROW_ALL_TD_FIELDS = "CRITERIA_170_315_A_1__C,Test Data,"
            + "Test data version,Test data alteration,Test data alteration description";

    private TestDataUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new TestDataUploadHandler(handlerUtil);
    }

    @Test
    public void parseTestData_NoTestDataColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        List<CertificationResultTestData> parsedTestDatas = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestDatas);
        assertEquals(0, parsedTestDatas.size());
    }

    @Test
    public void parseTestData_TestDataAllColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TD_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,,,,");
        assertNotNull(certResultRecords);

        List<CertificationResultTestData> parsedTestDatas = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestDatas);
        assertEquals(0, parsedTestDatas.size());
    }

    @Test
    public void parseTestData_TestDataNameAndVersionColumnsOnly_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test Data,Test data version").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,ONC Test Data,1.0");
        assertNotNull(certResultRecords);

        List<CertificationResultTestData> parsedTestDatas = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestDatas);
        assertEquals(1, parsedTestDatas.size());
        CertificationResultTestData td = parsedTestDatas.get(0);
        assertNotNull(td.getTestData());
        assertEquals("ONC Test Data", td.getTestData().getName());
        assertNull(td.getTestData().getId());
        assertEquals("1.0", td.getVersion());
        assertNull(td.getAlteration());
    }

    @Test
    public void parseTestData_TestDataWithoutAlterationBooleanColumn_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test Data,Test data version,Test data alteration description").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,ONC Test Data,1.0,An alteration");
        assertNotNull(certResultRecords);

        List<CertificationResultTestData> parsedTestDatas = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestDatas);
        assertEquals(1, parsedTestDatas.size());
        CertificationResultTestData td = parsedTestDatas.get(0);
        assertNotNull(td.getTestData());
        assertEquals("ONC Test Data", td.getTestData().getName());
        assertNull(td.getTestData().getId());
        assertEquals("1.0", td.getVersion());
        assertEquals("An alteration", td.getAlteration());
    }

    @Test
    public void parseTestData_MultipleTestDatasAllFieldsPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_TD_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,TD 1,1.0,1,Alteration" + "\n" + ",TD 2,4,,Alteration 2");
        assertNotNull(certResultRecords);

        List<CertificationResultTestData> parsedTestDatas = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestDatas);
        assertEquals(2, parsedTestDatas.size());
        parsedTestDatas.stream().forEach(td -> {
            assertNull(td.getId());
            assertNotNull(td.getTestData());
            if (td.getTestData().getName().equals("TD 1")) {
                assertEquals("1.0", td.getVersion());
                assertEquals("Alteration", td.getAlteration());
            } else if (td.getTestData().getName().equals("TD 2")) {
                assertEquals("4", td.getVersion());
                assertEquals("Alteration 2", td.getAlteration());
            } else {
                fail("No Test Data with name " + td.getTestData().getName() + " should have been found.");
            }
        });
    }

    @Test
    public void parseTestData_SingleTestDataUnexpectedHeaderOrder_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test data alteration description,Test data version,Test Data").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,An alteration,5,ONC Test Data");
        assertNotNull(certResultRecords);

        List<CertificationResultTestData> parsedTestDatas = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedTestDatas);
        assertEquals(1, parsedTestDatas.size());
        CertificationResultTestData td = parsedTestDatas.get(0);
        assertNotNull(td.getTestData());
        assertEquals("ONC Test Data", td.getTestData().getName());
        assertNull(td.getTestData().getId());
        assertEquals("5", td.getVersion());
        assertEquals("An alteration", td.getAlteration());
    }
}
