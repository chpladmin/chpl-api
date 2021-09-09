package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.TestTool;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestToolReviewerTest {
    private static final String TEST_TOOL_NOT_APPLICABLE = "Test tools are not applicable for the criterion %s.";
    private static final String TEST_TOOL_NOT_FOUND_REMOVED = "Criteria %s contains an invalid test tool '%s'. It has been removed from the pending listing.";
    private static final String TEST_TOOLS_MISSING = "Test tools are required for certification criteria %s.";
    private static final String MISSING_TEST_TOOL_NAME = "There was no test tool name found for certification criteria %s.";
    private static final String MISSING_TEST_TOOL_VERSION = "There was no version found for test tool %s and certification %s.";
    private static final String RETIRED_TEST_TOOL_NOT_ALLOWED = "Test Tool '%s' can not be used for criteria '%s', as it is a retired tool, and this Certified Product does not carry ICS.";
    private static final String TEST_TOOL_CRITERIA_MISMATCH = "Test Tool '%s' is not valid for criteria %s.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private TestToolDAO testToolDAO;
    private TestToolReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() throws EntityRetrievalException {
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil();
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
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
        Mockito.when(testToolDAO.getAllTestToolCriteriaMap()).thenReturn(getTestToolCriteriaMap());
        reviewer = new TestToolReviewer(certResultRules, chplProductNumberUtil, msgUtil, resourcePermissions, testToolDAO);
    }

    @Test
    public void review_nullTestToolsNoGapCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
    public void review_emptyTestToolsNoGapCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
    public void review_nullTestToolsWithGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
    public void review_testToolsNotApplicableToCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(false);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
                String.format(TEST_TOOL_NOT_APPLICABLE, "170.315 (a)(1)")));
    }

    @Test
    public void review_removesTestToolsWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolName("bad name")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_TOOL_NOT_FOUND_REMOVED, "170.315 (a)(1)", "bad name")));
    }

    @Test
    public void review_testToolWithoutNameNoId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolName("")
                .testToolVersion("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_TOOL_NOT_FOUND_REMOVED, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_testToolWithoutNameWithId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(2L)
                .testToolName("")
                .testToolVersion("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
    public void review_testToolMissingVersion_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(2L)
                .testToolName("missing version")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
    public void review_retiredTestToolWithoutListingIcs_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(2L)
                .testToolName("retired tool")
                .testToolVersion("1")
                .retired(true)
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.00.1.123456")
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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
    public void review_retiredTestToolWithIcs_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(2L)
                .testToolName("retired tool")
                .testToolVersion("1")
                .retired(true)
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.01.1.123456")
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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

    @Test
    public void review_retiredTestToolsWithAllData_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);

        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(2L)
                .testToolName("retired tool")
                .testToolVersion("1")
                .build());
        testTools.add(CertificationResultTestTool.builder()
                .testToolId(1L)
                .testToolName("good name")
                .testToolVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WErB.06.00.1.123456")
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
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

    private List<TestToolCriteriaMap> getTestToolCriteriaMap() {
        return Arrays.asList(
                TestToolCriteriaMap.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .certificationEdition("2015")
                                .certificationEditionId(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .testTool(new TestTool(1L, "good name"))
                        .build(),
                TestToolCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .certificationEdition("2015")
                        .certificationEditionId(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .testTool(new TestTool(2L, "bad name"))
                .build());
    }
}
