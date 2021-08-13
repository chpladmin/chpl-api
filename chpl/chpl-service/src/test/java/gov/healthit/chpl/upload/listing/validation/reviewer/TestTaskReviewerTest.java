package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

public class TestTaskReviewerTest {
    private static final String TEST_TASK_NOT_APPLICABLE = "Test Tasks are not applicable for the criterion %s.";
    private static final String MISSING_TEST_TASK = "Certification %s requires at least one test task.";
    private static final String TEST_TASK_FIELD_ROUNDED = "A non-integer numeric number was found in Test Task \"%s\" \"%s\" \"%s\". The number has been rounded to \"%s\".";

    private CertificationResultRules certResultRules;
    private CertificationCriterionService criteriaService;
    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;
    private CertificationCriterion a1, a2, a3;
    private TestTaskReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testTasksNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_TASK_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTask"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_TASK, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.roundedTestTaskNumber"),  ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(TEST_TASK_FIELD_ROUNDED, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));

        criteriaService = Mockito.mock(CertificationCriterionService.class);
        a1 = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("a1").build();
        a2 = CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").title("a2").build();
        a3 = CertificationCriterion.builder().id(3L).number("170.315 (a)(3)").title("a3").build();
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(a1.getId()))).thenReturn(a1);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(a2.getId()))).thenReturn(a2);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(a3.getId()))).thenReturn(a3);

        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(a1.getNumber()), ArgumentMatchers.eq(CertificationResultRules.TEST_TASK)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(a2.getNumber()), ArgumentMatchers.eq(CertificationResultRules.TEST_TASK)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(a3.getNumber()), ArgumentMatchers.eq(CertificationResultRules.TEST_TASK)))
            .thenReturn(false);

        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        reviewer = new TestTaskReviewer(criteriaService, new ValidationUtils(), certResultRules, "1,2",
                errorMessageUtil, resourcePermissions);
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
    public void review_testTaskHasNotAllowedCriteria_certResultHasSedTrue_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a3).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(TEST_TASK_NOT_APPLICABLE, "170.315 (a)(3)")));
    }

    @Test
    public void review_testTaskHasNotAllowedCriteria_certResultHasSedFalse_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a3).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(TEST_TASK_NOT_APPLICABLE, "170.315 (a)(3)")));
    }

    @Test
    public void review_testTaskHasNotAllowedCriteria_certResultUnattested_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a3).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(TEST_TASK_NOT_APPLICABLE, "170.315 (a)(3)")));
    }

    @Test
    public void review_criteriaWithSedIsMissingTestTask_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a2)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a2).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_TEST_TASK , Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_testTaskIncludesUnattestedCriteria_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(a1)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(TEST_TASK_NOT_APPLICABLE , Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullUniqueID_hasError() {
        String errMsg = "A test task for criteria %s is missing its unique ID.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskUniqueId"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask(null, Stream.of(a1).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_EmptyUniqueID_hasError() {
        String errMsg = "A test task for criteria %s is missing its unique ID.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskUniqueId"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask(null, Stream.of(a1).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_CriteriaNull_hasError() {
        String errMsg = "A test task with uniqueId %s is missing associated criteria.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskCriteria"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        listing.getSed().getTestTasks().get(0).setCriteria(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1")));
    }

    @Test
    public void review_CriteriaEmpty_hasError() {
        String errMsg = "A test task with uniqueId %s is missing associated criteria.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskCriteria"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", new ArrayList<CertificationCriterion>()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1")));
    }

    @Test
    public void review_LessThan10Participants_hasError() {
        String errMsg = "The test task %s for criteria %s requires at least 10 participants.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.badTestTaskParticipantsSize"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildTestTask("tt1", Stream.of(a1).collect(Collectors.toList()), 9));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_Exactly10Participants_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildTestTask("tt1", Stream.of(a1).collect(Collectors.toList()), 10));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_MoreThan10Participants_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildTestTask("tt1", Stream.of(a1).collect(Collectors.toList()), 11));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullDescription_hasError() {
        String errMsg = "The test task %s for criteria %s requires a description.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestDescription"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        listing.getSed().getTestTasks().get(0).setDescription(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_EmptyDescription_hasError() {
        String errMsg = "The test task %s for criteria %s requires a description.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestDescription"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        listing.getSed().getTestTasks().get(0).setDescription("");
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskSuccessAvg_NullTaskSuccessAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Success Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskSuccessAverage"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskSuccessAverage(null)
            .taskSuccessAverageStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskSuccessAvg_EmptyTaskSuccessAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Success Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskSuccessAverage"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskSuccessAverage(null)
            .taskSuccessAverageStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskSuccessAvg_NaNTaskSuccessAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Success Average value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskSuccessAverage"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskSuccessAverage(null)
            .taskSuccessAverageStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_NullTaskSuccessStddev_NullTaskSuccessStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Success Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskSuccessStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskSuccessStddev(null)
            .taskSuccessStddevStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskSuccessStddev_EmptyTaskSuccessStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Success Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskSuccessStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskSuccessStddev(null)
            .taskSuccessStddevStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskSuccessStddev_NaNTaskSuccessStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Success Standard Deviation value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskSuccessStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskSuccessStddev(null)
            .taskSuccessStddevStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_NullTaskPathDeviationObserved_NullTaskPathDeviationObservedStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Path Deviation Observed value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskPathDeviationObserved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationObserved(null)
            .taskPathDeviationObservedStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskPathDeviationObserved_EmptyTaskPathDeviationObservedStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Path Deviation Observed value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskPathDeviationObserved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationObserved(null)
            .taskPathDeviationObservedStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskPathDeviationObserved_NaNTaskPathDeviationObservedStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Path Deviation Observed value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskPathDeviationObserved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationObserved(null)
            .taskPathDeviationObservedStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_RoundedTaskPathDeviationObserved_NaNTaskPathDeviationObservedStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationObserved(1)
            .taskPathDeviationObservedStr("1.2")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_TASK_FIELD_ROUNDED, "tt1", "Task Path Deviation Observed", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullTaskPathDeviationOptimal_NullTaskPathDeviationOptimalStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Path Deviation Optimal value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskPathDeviationOptimal"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationOptimal(null)
            .taskPathDeviationOptimalStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskPathDeviationOptimal_EmptyTaskPathDeviationOptimalStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Path Deviation Optimal value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskPathDeviationOptimal"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationOptimal(null)
            .taskPathDeviationOptimalStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskPathDeviationOptimal_NaNTaskPathDeviationOptimalStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Path Deviation Optimal value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskPathDeviationOptimal"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationOptimal(null)
            .taskPathDeviationOptimalStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_RoundedTaskPathDeviationOptimal_NaNTaskPathDeviationOptimalStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskPathDeviationOptimal(1)
            .taskPathDeviationOptimalStr("1.2")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_TASK_FIELD_ROUNDED, "tt1", "Task Path Deviation Optimal", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullTaskTimeAverage_NullTaskTimeAverageStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeAvg(null)
            .taskTimeAvgStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeAverage_EmptyTaskTimeAverageStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeAvg(null)
            .taskTimeAvgStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeAverage_NaNTaskTimeAverageStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Time Average value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskTimeAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeAvg(null)
            .taskTimeAvgStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_RoundedTaskTimeAverage_NaNTaskTimeAverageStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeAvg(1L)
            .taskTimeAvgStr("1.2")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_TASK_FIELD_ROUNDED, "tt1", "Task Time Average", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullTaskTimeStddev_NullTaskTimeStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeStddev(null)
            .taskTimeStddevStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeStddev_EmptyTaskTimeStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeStddev(null)
            .taskTimeStddevStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeStddev_NaNTaskTimeStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Time Standard Devaition value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskTimeStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeStddev(null)
            .taskTimeStddevStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_RoundedTaskTimeStddev_NaNTaskTimeStddevStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeStddev(1)
            .taskTimeStddevStr("1.2")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_TASK_FIELD_ROUNDED, "tt1", "Task Time Standard Deviation", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullTaskTimeDeviationObservedAvg_NullTaskTimeDeviationObservedAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Deviation Observed Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeDeviationObservedAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationObservedAvg(null)
            .taskTimeDeviationObservedAvgStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeDeviationObservedAvg_EmptyTaskTimeDeviationObservedAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Deviation Observed Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeDeviationObservedAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationObservedAvg(null)
            .taskTimeDeviationObservedAvgStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeDeviationObservedAvg_NaNTaskTimeDeviationObservedAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Time Devaition Observed Average value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskTimeDeviationObservedAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationObservedAvg(null)
            .taskTimeDeviationObservedAvgStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_RoundedTaskTimeDeviationObservedAvg_NaNTaskTimeDeviationObservedAvgStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationObservedAvg(1)
            .taskTimeDeviationObservedAvgStr("1.2")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_TASK_FIELD_ROUNDED, "tt1", "Task Time Deviation Observed Average", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullTaskTimeDeviationOptimalAvg_NullTaskTimeDeviationOptimalAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Deviation Optimal Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeDeviationOptimalAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationOptimalAvg(null)
            .taskTimeDeviationOptimalAvgStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeDeviationOptimalAvg_EmptyTaskTimeDeviationOptimalAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Time Deviation Optimal Average value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskTimeDeviationOptimalAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationOptimalAvg(null)
            .taskTimeDeviationOptimalAvgStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskTimeDeviationOptimalAvg_NaNTaskTimeDeviationOptimalAvgStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Time Devaition Optimal Average value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskTimeDeviationOptimalAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationOptimalAvg(null)
            .taskTimeDeviationOptimalAvgStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_RoundedTaskTimeDeviationOptimalAvg_NaNTaskTimeDeviationOptimalAvgStr_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskTimeDeviationOptimalAvg(1)
            .taskTimeDeviationOptimalAvgStr("1.2")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(TEST_TASK_FIELD_ROUNDED, "tt1", "Task Time Deviation Optimal Average", "1.2", "1")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullTaskErrors_NullTaskErrorsStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Errors value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskErrors"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskErrors(null)
            .taskErrorsStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskErrors_EmptyTaskErrorsStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Errors value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskErrors"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskErrors(null)
            .taskErrorsStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskErrors_NaNTaskErrorsStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Errors value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskErrors"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskErrors(null)
            .taskErrorsStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_NullTaskErrorsStddev_NullTaskErrorsStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Errors Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskErrorsStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskErrorsStddev(null)
            .taskErrorsStddevStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskErrorsStddev_EmptyTaskErrorsStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Errors Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskErrorsStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskErrorsStddev(null)
            .taskErrorsStddevStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskErrorsStddev_NaNTaskErrorsStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Errors Standard Deviation value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskErrorsStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskErrorsStddev(null)
            .taskErrorsStddevStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_NullTaskRatingScale_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Rating Scale.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskRatingScale"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        listing.getSed().getTestTasks().get(0).setTaskRatingScale(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_EmptyTaskRatingScale_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Rating Scale.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskRatingScale"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        listing.getSed().getTestTasks().get(0).setTaskRatingScale("");
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskRating_NullTaskRatingStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Rating value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskRating"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskRating(null)
            .taskRatingStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskRating_EmptyTaskRatingStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Rating value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskRating"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskRating(null)
            .taskRatingStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskRating_NaNTaskRatingStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Rating value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskRating"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskRating(null)
            .taskRatingStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_NullTaskRatingStddev_NullTaskRatingStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Rating Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskRatingStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskRatingStddev(null)
            .taskRatingStddevStr(null)
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskRatingStddev_EmptyTaskRatingStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s requires a Task Rating Standard Deviation value.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTaskRatingStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskRatingStddev(null)
            .taskRatingStddevStr("")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1))));
    }

    @Test
    public void review_NullTaskRatingStddev_NaNTaskRatingStddevStr_hasError() {
        String errMsg = "The test task %s for criteria %s has an invalid Task Rating Standard Deviation value '%s'. This field must be a number.";
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestTaskRatingStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(errMsg, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1).collect(Collectors.toList())));
        TestTask modifiedTestTask = listing.getSed().getTestTasks().get(0).toBuilder()
            .taskRatingStddev(null)
            .taskRatingStddevStr("K")
            .build();
        listing.getSed().setTestTasks(Stream.of(modifiedTestTask).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(errMsg, "tt1", Util.formatCriteriaNumber(a1), "K")));
    }

    @Test
    public void review_testTaskValid_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a2)
                        .sed(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt1", Stream.of(a1, a2).collect(Collectors.toList())));
        listing.getSed().getTestTasks().add(
                buildValidTestTask("tt2", Stream.of(a2).collect(Collectors.toList())));
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private TestTask buildValidTestTask(String uniqueId, List<CertificationCriterion> criteria) {
        return buildTestTask(uniqueId, criteria, 10);
    }

    private TestTask buildTestTask(String uniqueId, List<CertificationCriterion> criteria, int tpCount) {
        TestTask tt = TestTask.builder()
                .uniqueId(uniqueId)
                .criteria(criteria)
                .description("desc")
                .taskErrors(1.5F)
                .taskErrorsStr("1.5")
                .taskErrorsStddev(2.5F)
                .taskErrorsStddevStr("2.5")
                .taskPathDeviationObserved(3)
                .taskPathDeviationObservedStr("3")
                .taskPathDeviationOptimal(4)
                .taskPathDeviationOptimalStr("4")
                .taskRating(5.5F)
                .taskRatingStr("5.5")
                .taskRatingScale("Likert")
                .taskRatingStddev(6.5F)
                .taskRatingStddevStr("6.5")
                .taskSuccessAverage(7.5F)
                .taskSuccessAverageStr("7.5")
                .taskSuccessStddev(8.5F)
                .taskSuccessStddevStr("8.5")
                .taskTimeAvg(9L)
                .taskTimeAvgStr("9")
                .taskTimeDeviationObservedAvg(10)
                .taskTimeDeviationObservedAvgStr("10")
                .taskTimeDeviationOptimalAvg(11)
                .taskTimeDeviationOptimalAvgStr("11")
                .taskTimeStddev(12)
                .taskTimeStddevStr("12")
        .build();
        tt.getTestParticipants().addAll(createTestParticipantCollection(tpCount));

        return tt;
    }

    private List<TestParticipant> createTestParticipantCollection(int size) {
        List<TestParticipant> tps = new ArrayList<TestParticipant>();
        for (int i = 0; i < size; i++) {
            tps.add(TestParticipant.builder().uniqueId("tp"+i).build());
        }
        return tps;
    }
}
