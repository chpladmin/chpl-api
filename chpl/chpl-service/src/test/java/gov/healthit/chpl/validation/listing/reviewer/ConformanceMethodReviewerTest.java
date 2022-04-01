package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class ConformanceMethodReviewerTest {
    private static final String CM_NOT_APPLICABLE_MSG = "Conformance Methods are not applicable for the criterion %s. They has been removed.";
    private static final String CM_REQUIRED_MSG = "Conformance Methods are required for certification criteria %s.";
    private static final String MISSING_CM_VERSION_MSG = "Conformance Method Version is required for certification %s with Conformance Method \"%s\".";
    private static final String UNALLOWED_CM_VERSION_MSG = "Conformance Method Version is not allowed for certification %s with Conformance Method \"%s\".";
    private static final String INVALID_CRITERIA_MSG = "Conformance Method \"%s\" is not valid for criteria %s.";

    private ConformanceMethodDAO conformanceMethodDAO;
    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certResultRules;
    private ResourcePermissions resourcePermissions;
    private ConformanceMethodReviewer conformanceMethodReviewer;
    private FF4j ff4j;

    @Before
    public void before() throws EntityRetrievalException {
        conformanceMethodDAO = Mockito.mock(ConformanceMethodDAO.class);
        Mockito.when(conformanceMethodDAO.getByCriterionId(1L)).thenReturn(getConformanceMethods());

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
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.invalidCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CRITERIA_MSG, i.getArgument(1), i.getArgument(2)));

        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(CertificationResultRules.CONFORMANCE_METHOD)))
            .thenReturn(true);

        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.CONFORMANCE_METHOD))
            .thenReturn(true);

        conformanceMethodReviewer = new ConformanceMethodReviewer(conformanceMethodDAO, msgUtil,
                new ValidationUtils(), certResultRules,
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
    public void review_conformanceMethodMayNotHaveVersion_hasError() {
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

    private List<ConformanceMethod> getConformanceMethods() {
        List<ConformanceMethod> cms = new ArrayList<ConformanceMethod>();
        cms.add(ConformanceMethod.builder()
                .id(1L)
                .name("Attestation")
                .build());
        cms.add(ConformanceMethod.builder()
                .id(2L)
                .name("ONC Test Procedure")
                .build());
        return cms;
    }

    private Map<String, Object> get2015CertificationEdition() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return certEdition;
    }
}
