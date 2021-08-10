package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class TestTaskReviewerTest {
    private static final String TEST_TASK_NOT_APPLICABLE = "Test Tasks are not applicable for the criterion %s.";
    private static final String MISSING_TEST_TASK = "Certification %s requires at least one test task.";

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
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        listing.getSed().setTestTasks(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestTasksNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
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
                .sed(CertifiedProductSed.builder()
                        .testTask(TestTask.builder()
                                .description("desc")
                                .criterion(a3)
                                .build())
                        .build())
                .build();
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
                .sed(CertifiedProductSed.builder()
                        .testTask(TestTask.builder()
                                .description("desc")
                                .criterion(a3)
                                .build())
                        .build())
                .build();
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
                .sed(CertifiedProductSed.builder()
                        .testTask(TestTask.builder()
                                .description("desc")
                                .criterion(a3)
                                .build())
                        .build())
                .build();
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
                .sed(CertifiedProductSed.builder()
                        .testTask(TestTask.builder()
                                .description("desc")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_TEST_TASK , "170.315 (a)(1)")));
    }

    @Test
    public void review_testTaskIncludesUnattestedCriteria_hasErrors() {
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
                .sed(CertifiedProductSed.builder()
                        .testTask(TestTask.builder()
                                .criterion(a1)
                                .description("desc")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(TEST_TASK_NOT_APPLICABLE , "170.315 (a)(1)")));
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
                .sed(CertifiedProductSed.builder()
                        .testTask(TestTask.builder()
                                .criterion(a1)
                                .criterion(a2)
                                .description("desc")
                                .build())
                        .testTask(TestTask.builder()
                                .criterion(a2)
                                .description("desc")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
