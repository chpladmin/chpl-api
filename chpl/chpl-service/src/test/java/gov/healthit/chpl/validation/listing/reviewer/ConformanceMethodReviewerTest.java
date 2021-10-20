package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ConformanceMethodReviewerTest {
    private static final String MISSING_CM_VERSION_ERROR_KEY = "listing.criteria.conformanceMethod.missingConformanceMethodVersion";
    private static final String UNALLOWED_CM_VERSION_ERROR_KEY = "listing.criteria.conformanceMethod.unallowedConformanceMethodVersion";
    private static final String INVALID_CRITERIA_ERROR_KEY = "listing.criteria.conformanceMethod.invalidCriteria";

    private ConformanceMethodDAO conformanceMethodDAO;
    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;
    private ConformanceMethodReviewer conformanceMethodReviewer;
    private FF4j ff4j;

    @Before
    public void before() throws EntityRetrievalException {
        conformanceMethodDAO = Mockito.mock(ConformanceMethodDAO.class);
        Mockito.when(conformanceMethodDAO.getAllConformanceMethodCriteriaMap())
        .thenReturn(getConformanceMethodCriteriaMaps());
        Mockito.when(conformanceMethodDAO.getByCriterionId(1L))
        .thenReturn(getConformanceMethods());

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(MISSING_CM_VERSION_ERROR_KEY))
        .thenReturn("Test Error Message 1");
        Mockito.when(errorMessageUtil.getMessage(UNALLOWED_CM_VERSION_ERROR_KEY))
        .thenReturn("Test Error Message 1");
        Mockito.when(errorMessageUtil.getMessage(INVALID_CRITERIA_ERROR_KEY))
        .thenReturn("Test Error Message 1");

        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.CONFORMANCE_METHOD))
        .thenReturn(true);

        conformanceMethodReviewer = new ConformanceMethodReviewer(conformanceMethodDAO, errorMessageUtil, resourcePermissions, ff4j);
    }

    @Test
    public void review_validConformanceMethodAndValidCriterion_NoErrors() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("cm1")
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
                .certificationEdition(certEdition)
                .build();

        conformanceMethodReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodMayNotHaveVersion_ErrorMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertificationResultConformanceMethod crcm = CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
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
                .certificationEdition(certEdition)
                .build();


        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_conformanceMethodMustHaveVersion_ErrorMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

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
                .certificationEdition(certEdition)
                .build();


        conformanceMethodReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    private List<ConformanceMethodCriteriaMap> getConformanceMethodCriteriaMaps() {
        List<ConformanceMethodCriteriaMap> map = new ArrayList<ConformanceMethodCriteriaMap>();

        map.add(ConformanceMethodCriteriaMap.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .certificationEdition("2015")
                        .certificationEditionId(3L)
                        .number("170.315 (a)(6)")
                        .removed(false)
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("cm1")
                        .build())
                .build());

        return map;
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
}
