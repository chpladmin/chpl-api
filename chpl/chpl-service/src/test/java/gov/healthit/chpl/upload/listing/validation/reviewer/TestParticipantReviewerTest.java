package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestParticipantReviewerTest {
    private static final String TEST_PARTICIPANT_FIELD_ROUNDED = "A non-integer numeric number was found in Test Participant \"%s\" \"%s\" \"%s\". The number has been rounded to \"%s\".";

    private ErrorMessageUtil errorMessageUtil;
    private TestParticipantReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.roundedParticipantNumber"),  ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(TEST_PARTICIPANT_FIELD_ROUNDED, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));

        reviewer = new TestParticipantReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullTestTasksNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().setTestTasks(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestTasksNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullTestParticipantsNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestParticipantsNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullUniqueID_hasError() {
        String errMsg = "A test participant is missing its unique ID.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestParticipantUniqueId")))
            .thenReturn(errMsg);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(buildValidTestParticipant(null)).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(errMsg));
    }

    @Test
    public void review_EmptyUniqueID_hasError() {
        String errMsg = "A test participant is missing its unique ID.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestParticipantUniqueId")))
            .thenReturn(errMsg);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(buildValidTestParticipant("")).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(errMsg));
    }

    @Test
    public void review_NullAgeRangeId_NullAgeRangeStr_hasError() {
        String errMsg = "Age range is required for participant %s but was not found.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantAgeRange"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                    .ageRange(null)
                    .ageRangeId(null)
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullAgeRangeId_EmptyAgeRangeStr_hasError() {
        String errMsg = "Age range is required for participant %s but was not found.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantAgeRange"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                    .ageRange("")
                    .ageRangeId(null)
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullAgeRangeId_InvalidAgeRangeStr_hasError() {
        String errMsg = "Age range %s for participant %s is an invalid value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidParticipantAgeRange"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                    .ageRange("notanagerange")
                    .ageRangeId(null)
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "notanagerange", "TP1")));
    }

    @Test
    public void review_NullEducationLevelId_NullEducationLevelStr_hasError() {
        String errMsg = "Education level is required for participant %s but was not found.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantEducationLevel"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                    .educationTypeName(null)
                    .educationTypeId(null)
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullEducationLevelId_EmptyEducationLevelStr_hasError() {
        String errMsg = "Education level is required for participant %s but was not found.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantEducationLevel"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                    .educationTypeName("")
                    .educationTypeId(null)
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullEducationLevelId_InvalidEducationLevelStr_hasError() {
        String errMsg = "Education level %s for participant %s is an invalid value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidParticipantEducationLevel"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .educationTypeName("notaneducation")
                .educationTypeId(null)
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "notaneducation", "TP1")));
    }

    @Test
    public void review_NullGender_hasError() {
        String errMsg = "Gender is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantGender"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .gender(null)
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        System.out.println(listing.getErrorMessages().iterator().next());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1")));
    }

    @Test
    public void review_EmptyGender_hasError() {
        String errMsg = "Gender is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantGender"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .gender("")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1")));
    }

    @Test
    public void review_NullOccupation_hasError() {
        String errMsg = "Occupation is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantOccupation"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .occupation(null)
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1")));
    }

    @Test
    public void review_EmptyOccupation_hasError() {
        String errMsg = "Occupation is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantOccupation"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .occupation("")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1")));
    }

    @Test
    public void review_NullAssistiveTechnologyNeeds_hasError() {
        String errMsg = "Assistive Technology Needs are required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantAssistiveTechnologyNeeds"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .assistiveTechnologyNeeds(null)
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1")));
    }

    @Test
    public void review_EmptyAssistiveTechnologyNeeds_hasError() {
        String errMsg = "Assistive Technology Needs are required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantAssistiveTechnologyNeeds"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .assistiveTechnologyNeeds("")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1")));
    }

    @Test
    public void review_NullProfessionalExperienceMonths_NullProfessionalExperienceMonthsStr_hasError() {
        String errMsg = "Professional Experience (in months) is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantProfessionalExperienceMonths"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .professionalExperienceMonths(null)
                .professionalExperienceMonthsStr(null)
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullProfessionalExperienceMonths_EmptyProfessionalExperienceMonthsStr_hasError() {
        String errMsg = "Professional Experience (in months) is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantProfessionalExperienceMonths"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .professionalExperienceMonths(null)
                .professionalExperienceMonthsStr("")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullProfessionalExperienceMonths_NaNProfessionalExperienceMonthsStr_hasError() {
        String errMsg = "Professional Experience (in months) has an invalid value '%s' for participant %s. The field must be a whole number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidParticipantProfessionalExperienceMonths"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .professionalExperienceMonths(null)
                .professionalExperienceMonthsStr("K")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "K", "TP1")));
    }

    @Test
    public void review_RoundedProfessionalExperieneMonths_NaNProfessionalExperienceMonthsStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .professionalExperienceMonths(1)
                .professionalExperienceMonthsStr("1.2")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_PARTICIPANT_FIELD_ROUNDED, "TP1", "Professional Experience Months", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullProductExperienceMonths_NullProductExperienceMonthsStr_hasError() {
        String errMsg = "Product Experience (in months) is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantProductExperienceMonths"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .productExperienceMonths(null)
                .productExperienceMonthsStr(null)
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullProductExperienceMonths_EmptyProductExperienceMonthsStr_hasError() {
        String errMsg = "Product Experience (in months) is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantProductExperienceMonths"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .productExperienceMonths(null)
                .productExperienceMonthsStr("")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullProductExperienceMonths_NaNProductExperienceMonthsStr_hasError() {
        String errMsg = "Product Experience (in months) has an invalid value '%s' for participant %s. The field must be a whole number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidParticipantProductExperienceMonths"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .productExperienceMonths(null)
                .productExperienceMonthsStr("K")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "K", "TP1")));
    }

    @Test
    public void review_RoundedProductExperieneMonths_NaNProductExperienceMonthsStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .productExperienceMonths(1)
                .productExperienceMonthsStr("1.2")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_PARTICIPANT_FIELD_ROUNDED, "TP1", "Product Experience Months", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullComputerExperienceMonths_NullComputerExperienceMonthsStr_hasError() {
        String errMsg = "Computer Experience (in months) is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantComputerExperienceMonths"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .computerExperienceMonths(null)
                .computerExperienceMonthsStr(null)
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullComputerExperienceMonths_EmptyComputerExperienceMonthsStr_hasError() {
        String errMsg = "Computer Experience (in months) is required for participant %s.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingParticipantComputerExperienceMonths"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .computerExperienceMonths(null)
                .computerExperienceMonthsStr("")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "TP1", "")));
    }

    @Test
    public void review_NullComputerExperienceMonths_NaNComputerExperienceMonthsStr_hasError() {
        String errMsg = "Computer Experience (in months) has an invalid value '%s' for participant %s. The field must be a whole number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidParticipantComputerExperienceMonths"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .computerExperienceMonths(null)
                .computerExperienceMonthsStr("K")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "K", "TP1")));
    }

    @Test
    public void review_RoundedComputerExperieneMonths_NaNComputerExperienceMonthsStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        TestParticipant testParticipant = buildValidTestParticipant("TP1").toBuilder()
                .computerExperienceMonths(1)
                .computerExperienceMonthsStr("1.2")
            .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(testParticipant).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_PARTICIPANT_FIELD_ROUNDED, "TP1", "Computer Experience Months", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testParticipantsValid_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(
                Stream.of(buildValidTestParticipant("TP1"), buildValidTestParticipant("TP2")).collect(Collectors.toSet()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private TestParticipant buildValidTestParticipant(String uniqueId) {
        return buildTestParticipant(uniqueId);
    }

    private TestParticipant buildTestParticipant(String uniqueId) {
        return TestParticipant.builder()
                .uniqueId(uniqueId)
                .ageRange("10-20")
                .ageRangeId(1L)
                .assistiveTechnologyNeeds("some needs")
                .computerExperienceMonths(24)
                .computerExperienceMonthsStr("24")
                .educationTypeId(2L)
                .educationTypeName("Bachelor's Degree")
                .gender("F")
                .occupation("Teacher")
                .productExperienceMonths(5)
                .productExperienceMonthsStr("5")
                .professionalExperienceMonths(10)
                .professionalExperienceMonthsStr("10")
        .build();
    }
}
