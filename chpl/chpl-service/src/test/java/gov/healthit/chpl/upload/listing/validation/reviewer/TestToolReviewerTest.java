package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.testtool.TestTool;
import gov.healthit.chpl.testtool.TestToolDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class TestToolReviewerTest {
    private static final String TEST_TOOL_NOT_APPLICABLE = "Test tools are not applicable for the criterion %s. They have been removed.";
    private static final String TEST_TOOL_NOT_FOUND_REMOVED = "Criteria %s contains an invalid test tool '%s'. It has been removed from the pending listing.";
    private static final String TEST_TOOLS_MISSING = "Test tools are required for certification criteria %s.";
    private static final String MISSING_TEST_TOOL_NAME = "There was no test tool name found for certification criteria %s.";
    private static final String MISSING_TEST_TOOL_VERSION = "There was no version found for test tool %s and certification %s.";
    private static final String RETIRED_TEST_TOOL_NOT_ALLOWED = "Test Tool '%s' can not be used for criteria '%s', as it is a retired tool, and this Certified Product does not carry ICS.";
    private static final String TEST_TOOL_CRITERIA_MISMATCH = "Test Tool '%s' is not valid for criteria %s.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private TestToolDAO testToolDAO;
    private TestToolReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() throws EntityRetrievalException {
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil();
        certResultRules = Mockito.mock(CertificationResultRules.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        testToolDAO = Mockito.mock(TestToolDAO.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testToolsNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_TOOL_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testToolNotFoundAndRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_TOOL_NOT_FOUND_REMOVED, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTool"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_TOOLS_MISSING, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestToolName"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_TOOL_NAME, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestToolVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_TOOL_VERSION, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.retiredTestToolNoIcsNotAllowed"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(RETIRED_TEST_TOOL_NOT_ALLOWED, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testToolCriterionMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_TOOL_CRITERIA_MISMATCH, i.getArgument(1), i.getArgument(2)));
        Mockito.when(testToolDAO.getAllTestToolCriteriaMaps()).thenReturn(getTestToolCriteriaMap());

        reviewer = new TestToolReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                chplProductNumberUtil, msgUtil, testToolDAO);
    }

    @Test
    public void review_nullTestToolsNoGapCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestToolsUsed(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_TOOLS_MISSING, "170.315 (a)(1)")));
    }

    @Test
    public void review_nullTestToolsRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(false)
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestToolsUsed(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestToolsNoGapCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_TOOLS_MISSING, "170.315 (a)(1)")));
    }

    @Test
    public void review_emptyTestToolsRemovedCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
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
    public void review_nullTestToolsWithGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(true)
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestToolsUsed(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestToolsWithGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
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
    public void review_criteriaDoesNotSupportTestTools_hasWarningAndTestToolsSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(false);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(true)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_TOOL_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getTestToolsUsed());
    }

    @Test
    public void review_removedCriteriaDoesNotSupportTestTools_noWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(false);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(true)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_removesTestToolsWithoutId_hasWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .value("bad name")
                        .build())
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_TOOL_NOT_FOUND_REMOVED, "170.315 (a)(1)", "bad name")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ignoresTestToolsWithoutIdForRemovedCriteria_noWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .value("bad name")
                        .build())
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testToolWithoutNameNoId_hasWarnig() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .value("")
                        .build())
                .version("")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_TOOL_NOT_FOUND_REMOVED, "170.315 (a)(1)", "")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testToolWithoutNameNoIdForRemovedCriteria_notRemovedAndNoWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .value("")
                        .build())
                .version("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testToolWithoutNameWithId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("")
                        .build())
                .version("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_TOOL_NAME, "170.315 (a)(1)")));
    }

    @Test
    public void review_testToolWithoutNameWithIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("")
                        .build())
                .version("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testToolMissingVersion_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("missing version")
                        .build())
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_TOOL_VERSION, "missing version", "170.315 (a)(1)")));
    }

    @Test
    public void review_testToolMissingVersionForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("missing version")
                        .build())
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_retiredTestToolWithoutListingIcs_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("retired tool")
                        .startDay(LocalDate.MIN)
                        .endDay(LocalDate.MIN.plusDays(1))
                        .build())
                .version("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.00.1.123456")
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(RETIRED_TEST_TOOL_NOT_ALLOWED, "retired tool", "170.315 (a)(1)")));
    }

    @Test
    public void review_retiredTestToolWithoutListingIcsForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("retired tool")
                        .startDay(LocalDate.MIN)
                        .endDay(LocalDate.MIN.plusDays(1))
                        .build())
                .version("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.00.1.123456")
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_retiredTestToolWithIcs_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("retired tool")
                        .startDay(LocalDate.MIN)
                        .endDay(LocalDate.MIN.plusDays(1))
                        .build())
                .version("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.01.1.123456")
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    /*
    @Test
    public void review_retiredTestToolsWithAllData_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(2L)
                .testToolName("retired tool")
                .testToolVersion("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("good name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.00.1.123456")
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
*/

    @Test
    public void review_testToolsNotApplicableForCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(3L)
                        .value("another name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .gap(true)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_TOOL_CRITERIA_MISMATCH, "another name", "170.315 (a)(1)")));
    }

    @Test
    public void review_testToolsNotApplicableForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(3L)
                        .value("another name")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .gap(true)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private List<TestToolCriteriaMap> getTestToolCriteriaMap() {
        return Arrays.asList(
                TestToolCriteriaMap.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .certificationEdition("2015")
                                .certificationEditionId(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .testTool(TestTool.builder()
                                .id(1L)
                                .value("good name")
                                .build())
                        .build(),
                TestToolCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .certificationEdition("2015")
                        .certificationEditionId(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("bad name")
                        .build())
                .build());
    }
}
