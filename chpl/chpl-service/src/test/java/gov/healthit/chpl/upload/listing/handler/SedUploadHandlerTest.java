package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SedUploadHandlerTest {
    private static final String LISTING_HEADER = "UNIQUE_CHPL_ID__C";
    private static final String TEST_PARTICIPANT_HEADER = "Participant Identifier,Participant Gender,"
            + "Participant Age,Participant Education,Participant Occupation/Role,Participant Professional Experience,"
            + "Participant Computer Experience,Participant Product Experience,Participant Assistive Technology Needs";
    private static final String TEST_PARTICIPANT_B2 = "B2,Male,40-49,Bachelor's Degree,Clinical Assistant,60,220,16,No";
    private static final String TEST_TASK_HEADER = "Task Identifier,Task Description,"
            + "Task Success - Mean (%),Task Success - Standard Deviation (%),Task Path Deviation - Observed #,"
            + "Task Path Deviation - Optimal #,Task Time - Mean (seconds),Task Time - Standard Deviation (seconds),"
            + "Task Time Deviation - Observed Seconds,Task Time Deviation - Optimal Seconds,Task Errors  Mean(%),"
            + "Task Errors - Standard Deviation (%),Task Rating - Scale Type,Task Rating,"
            + "Task Rating - Standard Deviation";
    private static final String TEST_TASK_A1 = "A1.1,"
            + "\"Enable a user to electronically record, change, and access the following order types (i) "
            + "Medications; (ii)Laboratory; and (iii) Radiology/imaging.\","
            + "90.24,6,7,5,80,10,16,14,17,3,Likert,3.2,2";
    private SedUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        CertificationCriterionUploadHandler criterionHandler = Mockito.mock(CertificationCriterionUploadHandler.class);
        Mockito.when(criterionHandler.handle(ArgumentMatchers.any()))
            .thenReturn(buildCriterion(1L, "170.315 (a)(1)", "a title"));
        TestTaskUploadHandler testTaskHandler = new TestTaskUploadHandler(handlerUtil);
        TestParticipantsUploadHandler participantHandler = new TestParticipantsUploadHandler(handlerUtil);
        UcdProcessUploadHandler ucdHandler = new UcdProcessUploadHandler(handlerUtil);
        handler = new SedUploadHandler(criterionHandler,
                testTaskHandler,
                participantHandler,
                ucdHandler,
                handlerUtil);
    }

    @Test
    public void parseTasks_NoCriteriaOrTaskColumns_ReturnsEmptySed() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(LISTING_HEADER).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("14.05.05");
        assertNotNull(listingRecords);

        CertifiedProductSed parsedSed = handler.parseAsSed(headingRecord, listingRecords);
        assertNotNull(parsedSed);
        assertNotNull(parsedSed.getTestTasks());
        assertEquals(0, parsedSed.getTestTasks().size());
        assertNotNull(parsedSed.getUcdProcesses());
        assertEquals(0, parsedSed.getUcdProcesses().size());
    }

    @Test
    public void parseTasks_NoTaskColumns_ReturnsEmptySed() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("14.05.05,1");
        assertNotNull(listingRecords);

        CertifiedProductSed parsedSed = handler.parseAsSed(headingRecord, listingRecords);
        assertNotNull(parsedSed);
        assertNotNull(parsedSed.getTestTasks());
        assertEquals(0, parsedSed.getTestTasks().size());
        assertNotNull(parsedSed.getUcdProcesses());
        assertEquals(0, parsedSed.getUcdProcesses().size());
    }

    @Test
    public void parseTasks_CriterionTaskButNoAvailableTaskColumns_ReturnsSedWithUniqueIds() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,CRITERIA_170_315_A_1__C,Task Identifier,Participant Identifier").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "14.05.05,1,A1.1,B2");
        assertNotNull(listingRecords);

        CertifiedProductSed parsedSed = handler.parseAsSed(headingRecord, listingRecords);
        assertNotNull(parsedSed);
        assertNotNull(parsedSed.getTestTasks());
        assertEquals(1, parsedSed.getTestTasks().size());
        TestTask parsedTestTask = parsedSed.getTestTasks().get(0);
        assertEquals("A1.1", parsedTestTask.getUniqueId());
        assertNotNull(parsedTestTask.getTestParticipants());
        assertEquals(1, parsedTestTask.getTestParticipants().size());
        TestParticipant parsedParticipant = parsedTestTask.getTestParticipants().iterator().next();
        assertEquals("B2", parsedParticipant.getUniqueId());
        assertNotNull(parsedSed.getUcdProcesses());
        assertEquals(0, parsedSed.getUcdProcesses().size());
    }

    @Test
    public void parseTasks_CriterionTaskMultipleParticipantsButNoAvailableTaskColumns_ReturnsSedWithUniqueIds() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,CRITERIA_170_315_A_1__C,Task Identifier,Participant Identifier").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "14.05.05,1,A1.1,B2;B3;B4");
        assertNotNull(listingRecords);

        CertifiedProductSed parsedSed = handler.parseAsSed(headingRecord, listingRecords);
        assertNotNull(parsedSed);
        assertNotNull(parsedSed.getTestTasks());
        assertEquals(1, parsedSed.getTestTasks().size());
        TestTask parsedTestTask = parsedSed.getTestTasks().get(0);
        assertEquals("A1.1", parsedTestTask.getUniqueId());
        assertNotNull(parsedTestTask.getTestParticipants());
        assertEquals(3, parsedTestTask.getTestParticipants().size());
        parsedTestTask.getTestParticipants().stream().forEach(participant -> {
           assertNotNull(participant.getUniqueId());
           assertTrue(participant.getUniqueId().equals("B2") || participant.getUniqueId().equals("B3")
                   || participant.getUniqueId().equals("B4"));
        });
        assertNotNull(parsedSed.getUcdProcesses());
        assertEquals(0, parsedSed.getUcdProcesses().size());
    }

    @Test
    public void parseTasks_CriterionTaskAndAvailableTaskColumns_ReturnsSedWithTaskData() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C," + TEST_TASK_HEADER
                + ",CRITERIA_170_315_A_1__C,Task Identifier,Participant Identifier").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "14.05.05," + TEST_TASK_A1 + ",1,A1.1,B2");
        assertNotNull(listingRecords);

        CertifiedProductSed parsedSed = handler.parseAsSed(headingRecord, listingRecords);
        assertNotNull(parsedSed);
        assertNotNull(parsedSed.getTestTasks());
        assertEquals(1, parsedSed.getTestTasks().size());
        TestTask parsedTestTask = parsedSed.getTestTasks().get(0);
        assertEquals("A1.1", parsedTestTask.getUniqueId());
        assertEquals("Enable a user to electronically record, change, and access the following order types "
                + "(i) Medications; (ii)Laboratory; and (iii) Radiology/imaging.", parsedTestTask.getDescription());
        assertEquals(90.24F, parsedTestTask.getTaskSuccessAverage());
        assertEquals(6, parsedTestTask.getTaskSuccessStddev());
        assertEquals(7, parsedTestTask.getTaskPathDeviationObserved());
        assertEquals(5, parsedTestTask.getTaskPathDeviationOptimal());
        assertEquals(80, parsedTestTask.getTaskTimeAvg());
        assertEquals(10, parsedTestTask.getTaskTimeStddev());
        assertEquals(16, parsedTestTask.getTaskTimeDeviationObservedAvg());
        assertEquals(14, parsedTestTask.getTaskTimeDeviationOptimalAvg());
        assertEquals(17, parsedTestTask.getTaskErrors());
        assertEquals(3, parsedTestTask.getTaskErrorsStddev());
        assertEquals("Likert", parsedTestTask.getTaskRatingScale());
        assertEquals(3.2F, parsedTestTask.getTaskRating());
        assertEquals(2, parsedTestTask.getTaskRatingStddev());
        assertNotNull(parsedTestTask.getTestParticipants());
        assertEquals(1, parsedTestTask.getTestParticipants().size());
        TestParticipant parsedParticipant = parsedTestTask.getTestParticipants().iterator().next();
        assertEquals("B2", parsedParticipant.getUniqueId());
        assertNotNull(parsedSed.getUcdProcesses());
        assertEquals(0, parsedSed.getUcdProcesses().size());
    }

    @Test
    public void parseTasks_CriterionParticipantAndAvailableParticipantColumns_ReturnsSedWithParticipantData() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C," + TEST_PARTICIPANT_HEADER
                + ",CRITERIA_170_315_A_1__C,Task Identifier,Participant Identifier").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "14.05.05," + TEST_PARTICIPANT_B2 + ",1,A1.1,B2");
        assertNotNull(listingRecords);

        CertifiedProductSed parsedSed = handler.parseAsSed(headingRecord, listingRecords);
        assertNotNull(parsedSed);
        assertNotNull(parsedSed.getTestTasks());
        assertEquals(1, parsedSed.getTestTasks().size());
        TestTask parsedTestTask = parsedSed.getTestTasks().get(0);
        assertEquals("A1.1", parsedTestTask.getUniqueId());
        assertNotNull(parsedTestTask.getTestParticipants());
        assertEquals(1, parsedTestTask.getTestParticipants().size());
        TestParticipant parsedParticipant = parsedTestTask.getTestParticipants().iterator().next();
        assertEquals("B2", parsedParticipant.getUniqueId());
        assertEquals("40-49", parsedParticipant.getAgeRange());
        assertNull(parsedParticipant.getAgeRangeId());
        assertEquals("No", parsedParticipant.getAssistiveTechnologyNeeds());
        assertEquals(220, parsedParticipant.getComputerExperienceMonths());
        assertNull(parsedParticipant.getEducationTypeId());
        assertEquals("Bachelor's Degree", parsedParticipant.getEducationTypeName());
        assertEquals("Male", parsedParticipant.getGender());
        assertEquals("Clinical Assistant", parsedParticipant.getOccupation());
        assertEquals(16, parsedParticipant.getProductExperienceMonths());
        assertEquals(60, parsedParticipant.getProfessionalExperienceMonths());
        assertNotNull(parsedSed.getUcdProcesses());
        assertEquals(0, parsedSed.getUcdProcesses().size());
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
          .build();
    }
}
