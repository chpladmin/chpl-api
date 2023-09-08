package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import gov.healthit.chpl.conformanceMethod.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.testtool.TestTool;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class ConformanceMethodReviewerTest {
    private static final String CM_NOT_APPLICABLE_MSG = "Conformance Methods are not applicable for the criterion %s. They have been removed.";
    private static final String CM_REQUIRED_MSG = "Conformance Methods are required for certification criteria %s.";
    private static final String MISSING_CM_VERSION_MSG = "Conformance Method Version is required for certification %s with Conformance Method \"%s\".";
    private static final String UNALLOWED_CM_VERSION_MSG = "Conformance Method Version is not allowed for certification %s with Conformance Method \"%s\".";
    private static final String UNALLOWED_CM_VERSION_REMOVED_MSG = "Conformance Method Version is not allowed for certification %s with Conformance Method \"%s\". The version \"%s\" was removed.";
    private static final String INVALID_FOR_CRITERIA_REMOVED_MSG = "Conformance Method \"%s\" is not valid for criteria %s. It has been removed.";
    private static final String INVALID_FOR_CRITERIA_REPLACED_MSG = "Conformance Method \"%s\" is not valid for criteria %s. It has been replaced with \"%s\".";
    private static final String REMOVED_CM_WITHOUT_ICS = "Criterion %s cannot use Conformance Method \"%s\" since the Conformance Method is removed and not inherited with ICS.";
    private static final String F3_GAP_MISMATCH_MSG = "Certification %s cannot use Conformance Method \"%s\" since GAP is %s.";
    private static final String F3_TEST_TOOLS_NOT_ALLOWED_MSG = "Certification %s cannot specify test tools when using Conformance Method %s. The test tools have been removed.";
    private static final String F3_TEST_DATA_NOT_ALLOWED_MSG = "Certification %s cannot specify test data when using Conformance Method %s. The test data has been removed.";
    private static final String DEFAULT_CM_ADDED_MSG = "Criterion %s requires a Conformance Method but none was found. \"%s\" was added.";

    private CertificationResultDAO certResultDao;
    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certResultRules;
    private ResourcePermissions resourcePermissions;
    private ConformanceMethodReviewer conformanceMethodReviewer;
    private CertificationEdition edition2015;

    @Before
    public void before() throws EntityRetrievalException {
        edition2015 = CertificationEdition.builder()
                .id(3L)
                .name("2015")
                .build();
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
                            .removalDate(null)
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
                            .removalDate(null)
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(getF3())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(1L)
                            .name("Attestation")
                            .removalDate(null)
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(getF3())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(2L)
                            .name("ONC Test Procedure")
                            .removalDate(null)
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
                            .removalDate(null)
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(CertificationCriterion.builder()
                            .number("170.315 (a)(9)")
                            .id(100L)
                            .removed(true)
                            .build())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(1L)
                            .name("Attestation")
                            .removalDate(null)
                            .build())
                        .build(),
                    ConformanceMethodCriteriaMap.builder()
                        .criterion(CertificationCriterion.builder()
                            .number("170.315 (c)(2)")
                            .id(10L)
                            .build())
                        .conformanceMethod(ConformanceMethod.builder()
                            .id(5L)
                            .name("NCQA eCQM Test Method")
                            .removalDate(LocalDate.parse("2022-06-01"))
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
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.invalidCriteriaRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_FOR_CRITERIA_REMOVED_MSG, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.invalidCriteriaReplaced"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_FOR_CRITERIA_REPLACED_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.conformanceMethod.criteria.conformanceMethodRemovedWithoutIcs"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(REMOVED_CM_WITHOUT_ICS, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.f3GapMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(F3_GAP_MISMATCH_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.f3RemovedTestTools"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(F3_TEST_TOOLS_NOT_ALLOWED_MSG, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.f3RemovedTestData"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(F3_TEST_DATA_NOT_ALLOWED_MSG, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.addedDefaultForCriterion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(DEFAULT_CM_ADDED_MSG, i.getArgument(1), i.getArgument(2)));

        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(CertificationResultRules.CONFORMANCE_METHOD)))
            .thenReturn(true);

        CertificationCriterionService criterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criterionService.get(ArgumentMatchers.eq(Criteria2015.F_3)))
            .thenReturn(getF3());

        certResultDao = Mockito.mock(CertificationResultDAO.class);
        conformanceMethodReviewer = new ConformanceMethodReviewer(conformanceMethodDao, certResultDao,
                msgUtil, new ValidationUtils(), certResultRules, criterionService,
                resourcePermissions);
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
                .edition(edition2015)
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
                .edition(edition2015)
                .build();
        listing.getCertificationResults().get(0).setConformanceMethods(new ArrayList<CertificationResultConformanceMethod>());
        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CM_REQUIRED_MSG, "170.315 (a)(1)")));
    }

    @Test
    public void review_conformanceMethodsNotApplicableForAttestedCriterion_hasWarningAndConformanceMethodsSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(),
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
                .edition(edition2015)
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CM_NOT_APPLICABLE_MSG, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods());
    }

    @Test
    public void review_conformanceMethodsNotApplicableForUnattestedCriterion_noWarningAndConformanceMethodsSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(),
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
                .edition(edition2015)
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods());
    }

    @Test
    public void review_invalidConformanceMethodForCriterionNoDefault_hasWarningAndRemovesConformanceMethod() {
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
                .edition(edition2015)
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(INVALID_FOR_CRITERIA_REMOVED_MSG, "bad CM", "170.315 (a)(1)")));
        assertEquals(0, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CM_REQUIRED_MSG, "170.315 (a)(1)")));
    }

    @Test
    public void review_nullConformanceMethodForCriterionNoDefault_hasError() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(null)
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
                                .removed(false)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .edition(edition2015)
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CM_REQUIRED_MSG, "170.315 (a)(1)")));
    }

    @Test
    public void review_invalidConformanceMethodForCriterionHasDefault_hasWarningAndReplacesConformanceMethodKeepingVersion() {
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
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .edition(edition2015)
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(2, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(INVALID_FOR_CRITERIA_REPLACED_MSG, "bad CM", "170.315 (a)(2)", "Attestation")));
        assertTrue(listing.getWarningMessages().contains(String.format(UNALLOWED_CM_VERSION_REMOVED_MSG, "170.315 (a)(2)", "Attestation", "version")));
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals("Attestation", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getName());
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethodVersion());
        assertEquals(0, listing.getErrorMessages().size());
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
                .edition(edition2015)
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
                .edition(edition2015)
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(UNALLOWED_CM_VERSION_REMOVED_MSG, "170.315 (a)(2)", "Attestation", "bad version")));
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethodVersion());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_removedConformanceMethodCertificationDateBeforeRemovalDate_noErrorsNoWarnings() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-05-01")))
                .edition(edition2015)
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_removedConformanceMethodCertificationDateAfterRemovalDateAndIcsNull_hasErrorsNoWarnings() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-06-02")))
                .edition(edition2015)
                .ics(null)
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(REMOVED_CM_WITHOUT_ICS, "170.315 (c)(2)", "NCQA eCQM Test Method")));
    }

    @Test
    public void review_removedConformanceMethodCertificationDateAfterRemovalDateAndIcsParentsNull_hasErrorsNoWarnings() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-06-02")))
                .edition(edition2015)
                .ics(InheritedCertificationStatus.builder()
                        .build())
                .build();
        listing.getIcs().setParents(null);
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(REMOVED_CM_WITHOUT_ICS, "170.315 (c)(2)", "NCQA eCQM Test Method")));
    }

    @Test
    public void review_removedConformanceMethodCertificationDateAfterRemovalDateAndIcsParentsEmpty_hasErrorsNoWarnings() {
        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-06-02")))
                .edition(edition2015)
                .ics(InheritedCertificationStatus.builder()
                        .parents(new ArrayList<CertifiedProduct>())
                        .build())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(REMOVED_CM_WITHOUT_ICS, "170.315 (c)(2)", "NCQA eCQM Test Method")));
    }

    @Test
    public void review_removedConformanceMethodCertificationDateAfterRemovalDateAndIcsParentQueryReturnsNull_hasErrorsNoWarnings() {
        Mockito.when(certResultDao.getConformanceMethodsByListingAndCriterionId(ArgumentMatchers.eq(1L), ArgumentMatchers.eq(10L)))
            .thenReturn(null);

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-06-02")))
                .edition(edition2015)
                .ics(InheritedCertificationStatus.builder()
                        .parents(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .build()).toList())
                        .build())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(REMOVED_CM_WITHOUT_ICS, "170.315 (c)(2)", "NCQA eCQM Test Method")));
    }

    @Test
    public void review_removedConformanceMethodCertificationDateAfterRemovalDateAndIcsParentQueryReturnsEmptyList_hasErrorsNoWarnings() {
        Mockito.when(certResultDao.getConformanceMethodsByListingAndCriterionId(ArgumentMatchers.eq(1L), ArgumentMatchers.eq(10L)))
            .thenReturn(new ArrayList<CertificationResultConformanceMethod>());

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-06-02")))
                .edition(edition2015)
                .ics(InheritedCertificationStatus.builder()
                        .parents(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .build()).toList())
                        .build())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(REMOVED_CM_WITHOUT_ICS, "170.315 (c)(2)", "NCQA eCQM Test Method")));
    }

    @Test
    public void review_removedConformanceMethodCertificationDateAfterRemovalDateAndIcsParentQueryReturnsDifferentConformanceMethod_hasErrorsNoWarnings() {
        Mockito.when(certResultDao.getConformanceMethodsByListingAndCriterionId(ArgumentMatchers.eq(1L), ArgumentMatchers.eq(10L)))
            .thenReturn(Stream.of(CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(3L)
                        .name("ONC Test Method")
                        .build())
                .conformanceMethodVersion("1")
                .build()).toList());

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-06-02")))
                .edition(edition2015)
                .ics(InheritedCertificationStatus.builder()
                        .parents(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .build()).toList())
                        .build())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(REMOVED_CM_WITHOUT_ICS, "170.315 (c)(2)", "NCQA eCQM Test Method")));
    }

    @Test
    public void review_removedConformanceMethodCertificationDateAfterRemovalDateAndIcsParentQueryReturnsSameConformanceMethod_noErrorsNoWarnings() {
        Mockito.when(certResultDao.getConformanceMethodsByListingAndCriterionId(ArgumentMatchers.eq(1L), ArgumentMatchers.eq(10L)))
            .thenReturn(Stream.of(CertificationResultConformanceMethod.builder()
                    .conformanceMethod(ConformanceMethod.builder()
                            .id(5L)
                            .name("NCQA eCQM Test Method")
                            .removalDate(LocalDate.parse("2022-06-01"))
                            .build())
                    .conformanceMethodVersion("1")
                    .build()).toList());

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .conformanceMethodVersion("1")
                .build();

        List<CertificationResultConformanceMethod> crcms = new ArrayList<CertificationResultConformanceMethod>();
        crcms.add(crcm);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (c)(2)")
                                .id(10L)
                                .build())
                        .conformanceMethods(crcms)
                        .build())
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-06-02")))
                .edition(edition2015)
                .ics(InheritedCertificationStatus.builder()
                        .parents(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .build()).toList())
                        .build())
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
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
                .edition(edition2015)
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
                .edition(edition2015)
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
                .edition(edition2015)
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
                .edition(edition2015)
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
                .edition(edition2015)
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
                                .testTool(TestTool.builder()
                                        .id(1L)
                                        .startDay(LocalDate.MIN)
                                        .endDay(LocalDate.MAX)
                                        .value("name")
                                        .build())
                                .version("1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .edition(edition2015)
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
                .edition(edition2015)
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
                                .testTool(TestTool.builder()
                                        .id(1L)
                                        .startDay(LocalDate.MIN)
                                        .endDay(LocalDate.MAX)
                                        .value("name")
                                        .build())
                                .version("1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .edition(edition2015)
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
                .edition(edition2015)
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
                .edition(edition2015)
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
                .edition(edition2015)
                .build();
        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyConformanceMethodAndCriteriaHasOneAllowed_DefaultPopulatedWithWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .removed(false)
                                .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        conformanceMethodReviewer.review(listing);
        assertNotNull(listing.getCertificationResults().get(0).getConformanceMethods());
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(DEFAULT_CM_ADDED_MSG, "170.315 (a)(2)", "Attestation")));
    }

    @Test
    public void review_emptyConformanceMethodAndCriteriaHasOneAllowed_RemovedCriteria_NonePopulatedNoWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(100L)
                                .number("170.315 (a)(9)")
                                .removed(true)
                                .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        conformanceMethodReviewer.review(listing);
        assertNotNull(listing.getCertificationResults().get(0).getConformanceMethods());
        assertEquals(0, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(0, listing.getWarningMessages().size());
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
                .edition(edition2015)
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
}
