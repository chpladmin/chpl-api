package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
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
    private TestDataReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        criteriaService = Mockito.mock(CertificationCriterionService.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
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
                criteriaService, msgUtil);
    }

    @Test
    public void review_nullTestDataNoGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestDataUsed(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestDataNoGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullTestDataWithGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
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
                        .gap(true)
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestDataUsed(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestDataWithGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
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
                        .gap(true)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataNotApplicableToCriteria_hasWarningAndTestDataSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(false);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name(TestDataDTO.DEFAULT_TEST_DATA)
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
                        .gap(true)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(false);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name(TestDataDTO.DEFAULT_TEST_DATA)
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
                        .gap(true)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataMissingOnG1WithoutGap_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
    public void review_testDataMissingOnG1RemovedWithoutGap_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(100L)
                                .number("170.315 (g)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .gap(false)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataMissingOnG1WithGap_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
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
                        .gap(true)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataMissingOnG2WithoutGap_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
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
                        .gap(false)
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
    public void review_testDataMissingOnG2RemovedWithoutGap_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(101L)
                                .number("170.315 (g)(2)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .gap(false)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataMissingOnG2WithGap_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
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
                        .gap(true)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validTestData_noErrors() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name(TestDataDTO.DEFAULT_TEST_DATA)
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
                        .gap(false)
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
