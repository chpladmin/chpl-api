package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class ConformanceMethodReviewerTest {
    private static final String CM_NOT_APPLICABLE_MSG = "Conformance Methods are not applicable for the criterion %s. They has been removed.";
    private static final String CM_REQUIRED_MSG = "Conformance Methods are required for certification criteria %s.";
    private static final String MISSING_CM_VERSION_MSG = "Conformance Method Version is required for certification %s with Conformance Method \"%s\".";
    private static final String UNALLOWED_CM_VERSION_MSG = "Conformance Method Version is not allowed for certification %s with Conformance Method \"%s\".";
    private static final String UNALLOWED_CM_VERSION_REMOVED_MSG = "Conformance Method Version is not allowed for certification %s with Conformance Method \"%s\". The version \"%s\" was removed.";
    private static final String INVALID_CRITERIA_MSG = "Conformance Method \"%s\" is not valid for criteria %s.";
    private static final String F3_GAP_MISMATCH_MSG = "Certification %s cannot use Conformance Method \"%s\" since GAP is %s.";
    private static final String F3_TEST_TOOLS_NOT_ALLOWED_MSG = "Certification %s cannot specify test tools when using Conformance Method %s. The test tools have been removed.";
    private static final String F3_TEST_DATA_NOT_ALLOWED_MSG = "Certification %s cannot specify test data when using Conformance Method %s. The test data has been removed.";

    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certResultRules;
    private ResourcePermissions resourcePermissions;
    private ConformanceMethodReviewer conformanceMethodReviewer;
    private FF4j ff4j;

    @Before
    public void before() throws EntityRetrievalException {
        ConformanceMethodDAO conformanceMethodDao = Mockito.mock(ConformanceMethodDAO.class);
        Mockito.when(conformanceMethodDao.getAllConformanceMethodCriteriaMap())
            .thenReturn(Stream.of(ConformanceMethodCriteriaMap.builder()
                        .criterion(CertificationCriterion.builder()
                            .number("170.315 (a)(1)")
                            .id(1L)
                            .build())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(1L)
                            .name("Attestation")
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(CertificationCriterion.builder()
                            .number("170.315 (a)(1)")
                            .id(1L)
                            .build())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(2L)
                            .name("ONC Test Procedure")
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(getF3())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(1L)
                            .name("Attestation")
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(getF3())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(2L)
                            .name("ONC Test Procedure")
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(CertificationCriterion.builder()
                            .number("170.315 (a)(2)")
                            .id(2L)
                            .build())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(1L)
                            .name("Attestation")
                            .build())
                        .build())
                    .toList());

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethodNotApplicable"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CM_NOT_APPLICABLE_MSG, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.missingConformanceMethod"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CM_REQUIRED_MSG, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.missingConformanceMethodVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_CM_VERSION_MSG, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.unallowedConformanceMethodVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(UNALLOWED_CM_VERSION_MSG, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.unallowedConformanceMethodVersionRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(UNALLOWED_CM_VERSION_REMOVED_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.invalidCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CRITERIA_MSG, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.f3GapMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(F3_GAP_MISMATCH_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.f3RemovedTestTools"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(F3_TEST_TOOLS_NOT_ALLOWED_MSG, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.f3RemovedTestData"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(F3_TEST_DATA_NOT_ALLOWED_MSG, i.getArgument(1), i.getArgument(2)));

        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(CertificationResultRules.CONFORMANCE_METHOD)))
            .thenReturn(true);

        CertificationCriterionService criterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criterionService.get(ArgumentMatchers.eq(Criteria2015.F_3)))
            .thenReturn(getF3());

        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.CONFORMANCE_METHOD))
            .thenReturn(true);

        conformanceMethodReviewer = new ConformanceMethodReviewer(conformanceMethodDao, msgUtil,
                new ValidationUtils(), certResultRules, criterionService,
                resourcePermissions, ff4j);
    }

    @Test
    public void review_conformanceMethodsRequiredButNull_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .conformanceMethods(null)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CM_REQUIRED_MSG, "170.315 (a)(1)")));
    }

    @Test
    public void review_conformanceMethodsRequiredButEmpty_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        listing.getCertificationResults().get(0).setConformanceMethods(new ArrayList<CertificationResultConformanceMethod>());
        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CM_REQUIRED_MSG, "170.315 (a)(1)")));
    }

    @Test
    public void review_conformanceMethodsNotApplicableForAttestedCriterion_hasWarningAndConformanceMethodsSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(CertificationResultRules.CONFORMANCE_METHOD)))
            .thenReturn(false);

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .conformanceMethodVersion("1.2")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CM_NOT_APPLICABLE_MSG, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods());
    }

    @Test
    public void review_conformanceMethodsNotApplicableForUnattestedCriterion_noWarningAndConformanceMethodsSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(CertificationResultRules.CONFORMANCE_METHOD)))
            .thenReturn(false);

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .conformanceMethodVersion("1.2")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods());
    }

    @Test
    public void review_invalidConformanceMethodForCriterion_hasWarningAndRemovesConformanceMethod() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(3L)
                        .name("bad CM")
                        .build())
                .conformanceMethodVersion("version")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(INVALID_CRITERIA_MSG, "bad CM", "170.315 (a)(1)")));
        assertEquals(0, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CM_REQUIRED_MSG, "170.315 (a)(1)")));
    }

    @Test
    public void review_conformanceMethodMayNotHaveVersionButCanHaveOtherConformanceMethods_hasError() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .conformanceMethodVersion("bad version")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(6)")
                                .id(1L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(UNALLOWED_CM_VERSION_MSG, "170.315 (a)(6)", "Attestation")));
    }

    @Test
    public void review_conformanceMethodMayNotHaveVersionAndCannotHaveOtherConformanceMethods_hasWarningAndVersionRemoved() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .conformanceMethodVersion("bad version")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(UNALLOWED_CM_VERSION_REMOVED_MSG, "170.315 (a)(2)", "Attestation", "bad version")));
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethodVersion());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodMustHaveVersion_hasError() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(6)")
                                .id(1L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CM_VERSION_MSG, "170.315 (a)(6)", "ONC Test Procedure")));
    }

    @Test
    public void review_conformanceMethodF3HasGapAndNonGapConformanceMethod_hasError() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .conformanceMethodVersion("1.1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(true)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(F3_GAP_MISMATCH_MSG, "170.315 (f)(3)", "ONC Test Procedure", "true")));
    }

    @Test
    public void review_conformanceMethodF3DoesNotHaveGapAndHasGapConformanceMethod_hasError() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(false)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(F3_GAP_MISMATCH_MSG, "170.315 (f)(3)", "Attestation", "false")));
    }

    @Test
    public void review_conformanceMethodF3DoesNotHaveGapAndHasNonGapConformanceMethod_noError() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .conformanceMethodVersion("1.1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(false)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodF3HasGapAndHasGapConformanceMethod_noError() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(true)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodF3HasGapAndGapConformanceMethodAndTestTools_hasWarningAndTestToolsRemoved() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(true)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .testToolsUsed(Stream.of(CertificationResultTestTool.builder()
                                .retired(false)
                                .testToolId(1L)
                                .testToolName("name")
                                .testToolVersion("1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(F3_TEST_TOOLS_NOT_ALLOWED_MSG, "170.315 (f)(3)", "Attestation")));
        assertEquals(0, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodF3HasGapAndGapConformanceMethodNoTestTools_noWarning() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(true)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodF3NotGapAndNotGapConformanceMethodAndTestTools_noWarning() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(false)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .testToolsUsed(Stream.of(CertificationResultTestTool.builder()
                                .retired(false)
                                .testToolId(1L)
                                .testToolName("name")
                                .testToolVersion("1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodF3HasGapAndGapConformanceMethodAndTestData_hasWarningAndTestDataRemoved() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(true)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .testDataUsed(Stream.of(CertificationResultTestData.builder()
                                .alteration("test")
                                .version("1")
                                .testData(TestData.builder()
                                        .id(1L)
                                        .name("td")
                                        .build())
                                .build()).collect(Collectors.toList()))
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(F3_TEST_DATA_NOT_ALLOWED_MSG, "170.315 (f)(3)", "Attestation")));
        assertEquals(0, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodF3HasGapAndGapConformanceMethodNoTestData_noWarning() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("Attestation")
                        .build())
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(true)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodF3NotGapAndNotGapConformanceMethodAndTestData_noWarning() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .gap(false)
                        .criterion(getF3())
                        .conformanceMethods(crcms)
                        .testDataUsed(Stream.of(CertificationResultTestData.builder()
                                .alteration("test")
                                .version("2")
                                .testData(TestData.builder()
                                        .id(2L)
                                        .name("td2")
                                        .build())
                                .build()).collect(Collectors.toList()))
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validConformanceMethodAndValidCriterion_noErrorsNoWarnings() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("ONC Test Procedure")
                        .build())
                .conformanceMethodVersion("version")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(6)")
                                .id(1L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationEdition(get2015CertificationEdition())
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterion getF3() {
        return CertificationCriterion.builder()
                .id(10L)
                .number("170.315 (f)(3)")
                .title("f3 title")
                .build();
    }

    private Map<String, Object> get2015CertificationEdition() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return certEdition;
    }
}
