package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestProcedureReviewerTest {
    private static final String TEST_PROCEDURE_NOT_APPLICABLE = "Test procedures are not applicable for the criterion %s.";
    private static final String TEST_PROCEDURE_NAME_INVALID = "Certification %s contains an invalid test procedure name: '%s'.";
    private static final String TEST_PROCEDURE_REQUIRED = "Test procedures are required for certification criteria %s.";
    private static final String MISSING_TEST_PROCEDURE_NAME = "Test procedure name is missing for certification %s.";
    private static final String MISSING_TEST_PROCEDURE_VERSION = "Test procedure version is required for certification %s.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private TestProcedureReviewer reviewer;
    private FF4j ff4j;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testProcedureNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_PROCEDURE_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestProcedure"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_PROCEDURE_REQUIRED, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.badTestProcedureName"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_PROCEDURE_NAME_INVALID, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestProcedureName"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_PROCEDURE_NAME, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestProcedureVersion"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_PROCEDURE_VERSION, i.getArgument(1), ""));

        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.CONFORMANCE_METHOD))
        .thenReturn(false);

        reviewer = new TestProcedureReviewer(certResultRules, msgUtil, resourcePermissions, ff4j);
    }

    @Test
    public void review_nullTestProceduresNoGapCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
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
        listing.getCertificationResults().get(0).setTestProcedures(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_PROCEDURE_REQUIRED, "170.315 (a)(1)")));
    }

    @Test
    public void review_emptyTestProceduresNoGapCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
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
                String.format(TEST_PROCEDURE_REQUIRED, "170.315 (a)(1)")));
    }

    @Test
    public void review_nullTestProceduresWithGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
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
        listing.getCertificationResults().get(0).setTestProcedures(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestProcedureWithGapCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
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
    public void review_testProcedureNotApplicableToCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(false);
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
                testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .id(1L)
                        .name(TestProcedureDTO.DEFAULT_TEST_PROCEDURE)
                        .build())
                .testProcedureVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(true)
                        .success(true)
                        .testProcedures(testProcedures)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_PROCEDURE_NOT_APPLICABLE, "170.315 (a)(1)")));
    }

    @Test
    public void review_testProcedureNullId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
                testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .id(null)
                        .name("bad name")
                        .build())
                .testProcedureVersion("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(true)
                        .success(true)
                        .testProcedures(testProcedures)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(TEST_PROCEDURE_NAME_INVALID, "170.315 (a)(1)", "bad name")));
    }

    @Test
    public void review_testProcedureEmptyName_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
            .testProcedure(TestProcedure.builder()
                    .id(1L)
                    .name("")
                    .build())
            .testProcedureVersion("1")
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
                        .success(true)
                        .testProcedures(testProcedures)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_PROCEDURE_NAME, "170.315 (a)(1)")));
    }

    @Test
    public void review_testProcedureNullName_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
            .testProcedure(TestProcedure.builder()
                    .id(1L)
                    .name(null)
                    .build())
            .testProcedureVersion("1")
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
                        .success(true)
                        .testProcedures(testProcedures)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_PROCEDURE_NAME, "170.315 (a)(1)")));
    }

    @Test
    public void review_testProcedureEmptyVersion_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
            .testProcedure(TestProcedure.builder()
                    .id(1L)
                    .name(TestProcedureDTO.DEFAULT_TEST_PROCEDURE)
                    .build())
            .testProcedureVersion("")
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
                        .success(true)
                        .testProcedures(testProcedures)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_PROCEDURE_VERSION, "170.315 (a)(1)")));
    }

    @Test
    public void review_testProcedureNullVersion_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
            .testProcedure(TestProcedure.builder()
                    .id(1L)
                    .name(TestProcedureDTO.DEFAULT_TEST_PROCEDURE)
                    .build())
            .testProcedureVersion(null)
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
                        .success(true)
                        .testProcedures(testProcedures)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_PROCEDURE_VERSION, "170.315 (a)(1)")));
    }

    @Test
    public void review_validTestProcedure_noErrors() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
            .testProcedure(TestProcedure.builder()
                    .id(1L)
                    .name(TestProcedureDTO.DEFAULT_TEST_PROCEDURE)
                    .build())
            .testProcedureVersion("1.1")
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .gap(false)
                        .success(true)
                        .testProcedures(testProcedures)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
