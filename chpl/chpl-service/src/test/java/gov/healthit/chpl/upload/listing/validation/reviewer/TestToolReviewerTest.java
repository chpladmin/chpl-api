package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestToolReviewerTest {
    private static final String TEST_TOOL_NOT_FOUND_REMOVED = "Criteria %s contains an invalid test tool '%s'. It has been removed from the pending listing.";
    private static final String TEST_TOOLS_MISSING = "Test tools are required for certification criteria %s.";
    private static final String MISSING_TEST_TOOL_NAME = "There was no test tool name found for certification criteria %s.";
    private static final String MISSING_TEST_TOOL_VERSION = "There was no version found for test tool %s and certification %s.";
    private static final String RETIRED_TEST_TOOL_NOT_ALLOWED = "Test Tool '%s' can not be used for criteria '%s', as it is a retired tool, and this Certified Product does not carry ICS.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private TestToolReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil();
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

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
        reviewer = new TestToolReviewer(certResultRules, chplProductNumberUtil, msgUtil, resourcePermissions);
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
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
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
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
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
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

//    @Test
//    public void review_hasNullQmsStandardName_hasError() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName(null)
//                        .qmsStandardId(null)
//                        .qmsModification(null)
//                        .applicableCriteria("test")
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getWarningMessages().size());
//        assertEquals(1, listing.getErrorMessages().size());
//        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
//    }
//
//    @Test
//    public void review_hasEmptyQmsStandardName_hasError() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName("")
//                        .qmsStandardId(null)
//                        .qmsModification(null)
//                        .applicableCriteria("test")
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getWarningMessages().size());
//        assertEquals(1, listing.getErrorMessages().size());
//        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
//    }
//
//    @Test
//    public void review_hasNullQmsStandardApplicableCriteria_hasError() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName("test")
//                        .qmsStandardId(null)
//                        .qmsModification(null)
//                        .applicableCriteria(null)
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getWarningMessages().size());
//        assertEquals(1, listing.getErrorMessages().size());
//        assertTrue(listing.getErrorMessages().contains(MISSING_APPLICABLE_CRITERIA));
//    }
//
//    @Test
//    public void review_hasEmptyQmsStandardApplicableCriteria_hasError() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName("test")
//                        .qmsStandardId(null)
//                        .qmsModification(null)
//                        .applicableCriteria("")
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getWarningMessages().size());
//        assertEquals(1, listing.getErrorMessages().size());
//        assertTrue(listing.getErrorMessages().contains(MISSING_APPLICABLE_CRITERIA));
//    }
//
//    @Test
//    public void review_hasQmsStandardNameAndApplicableCriteria_noError() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName("test")
//                        .qmsStandardId(null)
//                        .qmsModification(null)
//                        .applicableCriteria("ac")
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getWarningMessages().size());
//        assertEquals(0, listing.getErrorMessages().size());
//    }
//
//    @Test
//    public void review_hasQmsStandardNameAndApplicableCriteriaAndId_noError() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName("test")
//                        .qmsStandardId(2L)
//                        .qmsModification(null)
//                        .applicableCriteria("ac")
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getWarningMessages().size());
//        assertEquals(0, listing.getErrorMessages().size());
//    }
//
//    @Test
//    public void review_hasQmsStandardNameAndApplicableCriteriaAndIdAndModification_noError() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName("test")
//                        .qmsStandardId(2L)
//                        .qmsModification("mod")
//                        .applicableCriteria("ac")
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getWarningMessages().size());
//        assertEquals(0, listing.getErrorMessages().size());
//    }
//
//    @Test
//    public void review_hasQmsStandardNameNullIdFindsFuzzyMatch_hasWarning() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .qmsStandard(CertifiedProductQmsStandard.builder()
//                        .id(1L)
//                        .qmsStandardName("test")
//                        .userEnteredQmsStandardName("tst")
//                        .qmsStandardId(null)
//                        .qmsModification("mod")
//                        .applicableCriteria("ac")
//                        .build())
//                .build();
//        reviewer.review(listing);
//
//        assertEquals(0, listing.getErrorMessages().size());
//        assertEquals(1, listing.getWarningMessages().size());
//        assertTrue(listing.getWarningMessages().contains(String.format(FUZZY_MATCH_REPLACEMENT, FuzzyType.QMS_STANDARD.fuzzyType(), "tst", "test")));
//    }
}
