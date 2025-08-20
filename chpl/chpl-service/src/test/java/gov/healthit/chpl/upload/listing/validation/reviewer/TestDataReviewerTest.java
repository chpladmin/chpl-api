package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.testdata.CertificationResultTestData;
import gov.healthit.chpl.testdata.TestData;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class TestDataReviewerTest {
    private static final String TEST_DATA_NOT_APPLICABLE = "Test data is not applicable for the criterion %s. It has been removed.";
    private static final String TEST_DATA_NAME_INVALID = "Test data '%s' is invalid for the criterion %s and has been removed from the listing.";
    private static final String TEST_DATA_REQUIRED = "Test data is required for certification %s.";
    private static final String MISSING_TEST_DATA_NAME = "Test data name was not provided for certification %s.";
    private static final String MISSING_TEST_DATA_VERSION = "Test data version is required for certification %s.";

    private CertificationResultRules certResultRules;
    private CertificationCriterionService criteriaService;
    private ErrorMessageUtil msgUtil;
    private Long gapConformanceMethodId;
    private TestDataReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        criteriaService = Mockito.mock(CertificationCriterionService.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        gapConformanceMethodId = 1L;
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.G_1)))
            .thenReturn(CertificationCriterion.builder()
                    .id(100L)
                    .number("170.315 (g)(1)")
                    .build());
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.G_2)))
        .thenReturn(CertificationCriterion.builder()
                .id(101L)
                .number("170.315 (g)(2)")
                .startDay(LocalDate.parse("2023-05-01"))
                .certificationEdition("2015")
                .build());
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testDataNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_DATA_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidTestDataRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_DATA_NAME_INVALID, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testDataRequired"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_DATA_REQUIRED, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestDataName"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_DATA_NAME, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestDataVersion"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_DATA_VERSION, i.getArgument(1), ""));
        reviewer = new TestDataReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                criteriaService, msgUtil, gapConformanceMethodId);
    }

    @Test
    public void review_nullTestDataA1_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestDataUsed(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestDataA1_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataNotApplicableToCriteria_hasWarningAndTestDataSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(false);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name(TestData.DEFAULT_TEST_DATA)
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_DATA_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getTestDataUsed());
    }

    @Test
    public void review_testDataNotApplicableToRemovedCriteria_noWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(false);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name(TestData.DEFAULT_TEST_DATA)
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_testDataNameInvalid_hasWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(null)
                        .name("bad test data")
                        .build())
                .version("1")
                .build());
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        assertEquals(2, listing.getCertificationResults().get(0).getTestDataUsed().size());
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_DATA_NAME_INVALID, "bad test data", "170.315 (a)(1)")));
    }

    @Test
    public void review_testDataNameInvalidForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(null)
                        .name("bad name")
                        .build())
                .version("1")
                .build());
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataEmptyName_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(null)
                        .name("")
                        .build())
                .version("1")
                .build());
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_DATA_NAME, "170.315 (a)(1)")));
    }

    @Test
    public void review_testDataEmptyNameRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(null)
                        .name("")
                        .build())
                .version("1")
                .build());
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataEmptyVersion_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version("")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_DATA_VERSION, "170.315 (a)(1)")));
    }

    @Test
    public void review_testDataEmptyVersionForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version("")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataNullVersion_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version(null)
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_DATA_VERSION, "170.315 (a)(1)")));
    }

    @Test
    public void review_testDataNullVersionForRemovedCritiria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(2L)
                        .name("Valid Test Data")
                        .build())
                .version(null)
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataMissingOnG1_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(100L)
                                .number("170.315 (g)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_DATA_REQUIRED, "170.315 (g)(1)")));
    }

    @Test
    public void review_testDataMissingOnG2_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(101L)
                                .number("170.315 (g)(2)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_DATA_REQUIRED, "170.315 (g)(2)")));
    }

    @Test
    public void review_testDataMissingOnG1WithGapConformanceMethod_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(100L)
                                .number("170.315 (g)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .conformanceMethods(Stream.of(CertificationResultConformanceMethod.builder()
                                .conformanceMethod(ConformanceMethod.builder()
                                        .id(gapConformanceMethodId)
                                        .build())
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataMissingOnG2WithGapConformanceMethod_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(101L)
                                .number("170.315 (g)(2)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .conformanceMethods(Stream.of(CertificationResultConformanceMethod.builder()
                                .conformanceMethod(ConformanceMethod.builder()
                                        .id(gapConformanceMethodId)
                                        .build())
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validTestData_noErrors() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name(TestData.DEFAULT_TEST_DATA)
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
