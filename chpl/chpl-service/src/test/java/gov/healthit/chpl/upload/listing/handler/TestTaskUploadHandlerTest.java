package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestTaskUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C";
    private static final String HEADER_ROW = HEADER_ROW_BEGIN + ",Task Identifier,Task Description,"
            + "Task Success - Mean (%),Task Success - Standard Deviation (%),Task Path Deviation - Observed #,"
            + "Task Path Deviation - Optimal #,Task Time - Mean (seconds),Task Time - Standard Deviation (seconds),"
            + "Task Time Deviation - Observed Seconds,Task Time Deviation - Optimal Seconds,Task Errors  Mean(%),"
            + "Task Errors - Standard Deviation (%),Task Rating - Scale Type,Task Rating,"
            + "Task Rating - Standard Deviation";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";
    private static final String TEST_TASK_A1 = "A1.1,"
            + "\"Enable a user to electronically record, change, and access the following order types (i) "
            + "Medications; (ii)Laboratory; and (iii) Radiology/imaging.\","
            + "90.24,6,7,5,80,10,16,14,17,3,Likert,3.2,2";
    private static final String TEST_TASK_A2 = "A2.1,Task for (a)(2),44.32,6,7,5,80,10,16,14,17,3,"
            + "System Usability Scale,88,1";

    private TestTaskUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new TestTaskUploadHandler(handlerUtil);
    }

    @Test
    public void parseTasks_NoTaskColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(0, parsedTasks.size());
    }

    @Test
    public void parseTasks_TaskColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,,,,,,,,,,,,,,");
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(0, parsedTasks.size());
    }

    @Test
    public void parseTasks_TaskIdColumnOnly_ParsesField() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Task Identifier").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",A1.1");
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(1, parsedTasks.size());
        TestTask task = parsedTasks.get(0);
        assertEquals("A1.1", task.getUniqueId());
    }

    @Test
    public void parseTasks_FewColumnsDifferentOrder_ParsesFields() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Task Time Deviation - Optimal Seconds,Task Rating - Scale Type,"
                + "Task Identifier,Task Rating,Task Errors  Mean(%)").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",2,something,A1.1,5.6,7");
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(1, parsedTasks.size());
        TestTask task = parsedTasks.get(0);
        assertEquals("A1.1", task.getUniqueId());
        assertNull(task.getDescription());
        assertEquals(7, task.getTaskErrors());
        assertNull(task.getTaskErrorsStddev());
        assertNull(task.getTaskPathDeviationObserved());
        assertNull(task.getTaskPathDeviationOptimal());
        assertEquals(5.6F, task.getTaskRating());
        assertEquals("something", task.getTaskRatingScale());
        assertNull(task.getTaskRatingStddev());
        assertNull(task.getTaskSuccessAverage());
        assertNull(task.getTaskSuccessStddev());
        assertNull(task.getTaskTimeAvg());
        assertNull(task.getTaskTimeDeviationObservedAvg());
        assertEquals(2, task.getTaskTimeDeviationOptimalAvg());
        assertNull(task.getTaskTimeStddev());
    }

    @Test
    public void parseTasks_SingleTaskAndBlankLine_ParsesFields() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Task Time Deviation - Optimal Seconds,Task Rating - Scale Type,"
                + "Task Identifier,Task Rating,Task Errors  Mean(%)").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",2,something,A1.1,5.6,7\n"
                        + ",,,,,");
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(1, parsedTasks.size());
    }

    @Test
    public void parseTasks_InvalidNumberInFloatField_SetsFieldNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Task Rating").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",JUNK");
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(1, parsedTasks.size());
        TestTask task = parsedTasks.get(0);
        assertNull(task.getTaskRating());
    }

    @Test
    public void parseTasks_InvalidNumberInIntegerField_SetsFieldNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Task Path Deviation - Observed #").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",JUNK");
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(1, parsedTasks.size());
        TestTask task = parsedTasks.get(0);
        assertNull(task.getTaskPathDeviationObserved());
    }

    @Test
    public void parseTasks_FloatValueInIntegerField_RoundsToInteger() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Task Path Deviation - Observed #").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",1.3");
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(1, parsedTasks.size());
        TestTask task = parsedTasks.get(0);
        assertEquals(1, task.getTaskPathDeviationObserved());
    }

    @Test
    public void parseTasks_MultipleTasks_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + "," + TEST_TASK_A1 + "\n"
                        + LISTING_ROW_BEGIN + "," + TEST_TASK_A2);
        assertNotNull(listingRecords);

        List<TestTask> parsedTasks = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedTasks);
        assertEquals(2, parsedTasks.size());
        parsedTasks.stream().forEach(task -> {
            assertNull(task.getId());
            assertNotNull(task.getUniqueId());
            if (task.getUniqueId().equals("A1.1")) {
                assertEquals("Enable a user to electronically record, change, and access the following order types "
                        + "(i) Medications; (ii)Laboratory; and (iii) Radiology/imaging.", task.getDescription());
                assertEquals(90.24F, task.getTaskSuccessAverage());
                assertEquals(6, task.getTaskSuccessStddev());
                assertEquals(7, task.getTaskPathDeviationObserved());
                assertEquals(5, task.getTaskPathDeviationOptimal());
                assertEquals(80, task.getTaskTimeAvg());
                assertEquals(10, task.getTaskTimeStddev());
                assertEquals(16, task.getTaskTimeDeviationObservedAvg());
                assertEquals(14, task.getTaskTimeDeviationOptimalAvg());
                assertEquals(17, task.getTaskErrors());
                assertEquals(3, task.getTaskErrorsStddev());
                assertEquals("Likert", task.getTaskRatingScale());
                assertEquals(3.2F, task.getTaskRating());
                assertEquals(2, task.getTaskRatingStddev());
            } else if (task.getUniqueId().equals("A2.1")) {
                assertEquals("Task for (a)(2)", task.getDescription());
                assertEquals(44.32F, task.getTaskSuccessAverage());
                assertEquals(6, task.getTaskSuccessStddev());
                assertEquals(7, task.getTaskPathDeviationObserved());
                assertEquals(5, task.getTaskPathDeviationOptimal());
                assertEquals(80, task.getTaskTimeAvg());
                assertEquals(10, task.getTaskTimeStddev());
                assertEquals(16, task.getTaskTimeDeviationObservedAvg());
                assertEquals(14, task.getTaskTimeDeviationOptimalAvg());
                assertEquals(17, task.getTaskErrors());
                assertEquals(3, task.getTaskErrorsStddev());
                assertEquals("System Usability Scale", task.getTaskRatingScale());
                assertEquals(88F, task.getTaskRating());
                assertEquals(1, task.getTaskRatingStddev());
            } else {
                fail("No test task with unique id '" + task.getUniqueId() + "' should have been found.");
            }
        });
    }
}
