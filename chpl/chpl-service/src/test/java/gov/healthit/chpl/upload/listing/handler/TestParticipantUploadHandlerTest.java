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

import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestParticipantUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C";
    private static final String HEADER_ROW = HEADER_ROW_BEGIN + ",Participant Identifier,Participant Gender,"
            + "Participant Age,Participant Education,Participant Occupation/Role,Participant Professional Experience,"
            + "Participant Computer Experience,Participant Product Experience,Participant Assistive Technology Needs";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";
    private static final String TEST_PARTICIPANT_1 = "ID01,Male,40-49,Bachelor's Degree,Clinical Assistant,60,220,16,No";
    private static final String TEST_PARTICIPANT_2 = "ID02,Female,30-39,Bachelor's Degree,Clinical Assistant,60,220,16,No";

    private TestParticipantsUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new TestParticipantsUploadHandler(handlerUtil);
    }

    @Test
    public void parseParticipants_NoParticipantColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        List<TestParticipant> parsedParticipants = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedParticipants);
        assertEquals(0, parsedParticipants.size());
    }

    @Test
    public void parseParticipants_ParticipantColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,,,,,,,,");
        assertNotNull(listingRecords);

        List<TestParticipant> parsedParticipants = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedParticipants);
        assertEquals(0, parsedParticipants.size());
    }

    @Test
    public void parseParticipants_FewColumnsDifferentOrder_ParsesFields() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Participant Assistive Technology Needs,Participant Occupation/Role,Participant Age,"
                + "Participant Gender").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",None,Engineer,10-19,Male");
        assertNotNull(listingRecords);

        List<TestParticipant> parsedParticipants = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedParticipants);
        assertEquals(1, parsedParticipants.size());
        TestParticipant participant = parsedParticipants.get(0);
        assertNull(participant.getUniqueId());
        assertEquals("10-19", participant.getAgeRange());
        assertNull(participant.getAgeRangeId());
        assertEquals("None", participant.getAssistiveTechnologyNeeds());
        assertNull(participant.getComputerExperienceMonths());
        assertNull(participant.getEducationTypeId());
        assertNull(participant.getEducationTypeName());
        assertEquals("Male", participant.getGender());
        assertEquals("Engineer", participant.getOccupation());
        assertNull(participant.getProductExperienceMonths());
        assertNull(participant.getProfessionalExperienceMonths());
    }

    @Test
    public void parseParticipants_ParticipantIdColumnOnly_ParsesField() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Participant Identifier").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",A1.1");
        assertNotNull(listingRecords);

        List<TestParticipant> parsedParticipants = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedParticipants);
        assertEquals(1, parsedParticipants.size());
        TestParticipant participant = parsedParticipants.get(0);
        assertEquals("A1.1", participant.getUniqueId());
    }

    @Test
    public void parseParticipants_InvalidNumberInIntegerField_SetsFieldNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Participant Computer Experience").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",JUNK");
        assertNotNull(listingRecords);

        List<TestParticipant> parsedParticipants = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedParticipants);
        assertEquals(1, parsedParticipants.size());
        TestParticipant participant = parsedParticipants.get(0);
        assertNull(participant.getComputerExperienceMonths());
    }

    @Test
    public void parseParticipants_SingleParticipantAndBlankLine_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + "," + TEST_PARTICIPANT_1 + "\n"
                        + LISTING_ROW_BEGIN + ",,,,,,,,,");
        assertNotNull(listingRecords);

        List<TestParticipant> parsedParticipants = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedParticipants);
        assertEquals(1, parsedParticipants.size());
    }

    @Test
    public void parseParticipants_MultipleParticipants_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + "," + TEST_PARTICIPANT_1 + "\n"
                        + LISTING_ROW_BEGIN + "," + TEST_PARTICIPANT_2);
        assertNotNull(listingRecords);

        List<TestParticipant> parsedParticipants = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedParticipants);
        assertEquals(2, parsedParticipants.size());
        parsedParticipants.stream().forEach(participant -> {
            assertNull(participant.getId());
            assertNotNull(participant.getUniqueId());
            if (participant.getUniqueId().equals("ID01")) {
                assertEquals("40-49", participant.getAgeRange());
                assertNull(participant.getAgeRangeId());
                assertEquals("No", participant.getAssistiveTechnologyNeeds());
                assertEquals(220, participant.getComputerExperienceMonths());
                assertNull(participant.getEducationTypeId());
                assertEquals("Bachelor's Degree", participant.getEducationTypeName());
                assertEquals("Male", participant.getGender());
                assertEquals("Clinical Assistant", participant.getOccupation());
                assertEquals(16, participant.getProductExperienceMonths());
                assertEquals(60, participant.getProfessionalExperienceMonths());
            } else if (participant.getUniqueId().equals("ID02")) {
                assertEquals("30-39", participant.getAgeRange());
                assertNull(participant.getAgeRangeId());
                assertEquals("No", participant.getAssistiveTechnologyNeeds());
                assertEquals(220, participant.getComputerExperienceMonths());
                assertNull(participant.getEducationTypeId());
                assertEquals("Bachelor's Degree", participant.getEducationTypeName());
                assertEquals("Female", participant.getGender());
                assertEquals("Clinical Assistant", participant.getOccupation());
                assertEquals(16, participant.getProductExperienceMonths());
                assertEquals(60, participant.getProfessionalExperienceMonths());
            } else {
                fail("No test participant with unique id '" + participant.getUniqueId() + "' should have been found.");
            }
        });
    }
}
