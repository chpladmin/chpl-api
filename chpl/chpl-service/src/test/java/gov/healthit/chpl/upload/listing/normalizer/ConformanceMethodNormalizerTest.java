package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ConformanceMethodNormalizerTest {
    private static final String DEFAULT_CM_ADDED_MSG = "Criterion %s requires a Conformance Method but none was found. \"%s\" was added.";

    private ConformanceMethodDAO cmDao;
    private ConformanceMethodNormalizer normalizer;

    @Before
    public void before() {
        cmDao = Mockito.mock(ConformanceMethodDAO.class);
        List<ConformanceMethodCriteriaMap> allowedCms = new ArrayList<ConformanceMethodCriteriaMap>();
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("CM 1")
                        .build())
                .build());
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("CM 2")
                        .build())
                .build());
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315 (b)(1)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("CM 1")
                        .build())
                .build());
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(10L)
                        .number("170.315 (c)(2)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .build());

        try {
            Mockito.when(cmDao.getAllConformanceMethodCriteriaMap()).thenReturn(allowedCms);
        } catch (EntityRetrievalException e) {
        }

        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.conformanceMethod.addedDefaultForCriterion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(DEFAULT_CM_ADDED_MSG, i.getArgument(1), i.getArgument(2)));

        normalizer = new ConformanceMethodNormalizer(cmDao, msgUtil);
    }

    @Test
    public void normalize_emptyConformanceMethodAndCriteriaHasMultipleAllowed_DefaultNotPopulated() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getConformanceMethods());
        assertEquals(0, listing.getCertificationResults().get(0).getConformanceMethods().size());
    }

    @Test
    public void normalize_emptyConformanceMethodAndCriteriaHasOneAllowed_DefaultPopulatedWithWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getConformanceMethods());
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(DEFAULT_CM_ADDED_MSG, "170.315 (b)(1)", "CM 1")));
    }

    @Test
    public void normalize_emptyConformanceMethodAndUnattestedCriteriaHasOneAllowed_NoDefaultPopulated() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getConformanceMethods());
        assertEquals(0, listing.getCertificationResults().get(0).getConformanceMethods().size());
    }

    @Test
    public void normalize_conformanceMethodNotAllowedForCriterion_idIsNull() {
        List<CertificationResultConformanceMethod> cms = new ArrayList<CertificationResultConformanceMethod>();
        cms.add(CertificationResultConformanceMethod.builder()
                .id(null)
                .conformanceMethod(ConformanceMethod.builder()
                        .id(null)
                        .name("notindb")
                        .build())
                .conformanceMethodVersion("100")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .conformanceMethods(cms)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getId());
        assertEquals("notindb", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getName());
    }

    @Test
    public void normalize_conformanceMethodAllowedForCriterion_setsIdField() {
        List<CertificationResultConformanceMethod> cms = new ArrayList<CertificationResultConformanceMethod>();
        cms.add(CertificationResultConformanceMethod.builder()
                .id(null)
                .conformanceMethod(ConformanceMethod.builder()
                        .id(null)
                        .name("CM 1")
                        .build())
                .conformanceMethodVersion("100")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .conformanceMethods(cms)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getId());
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getRemovalDate());
        assertEquals("CM 1", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getName());
        assertEquals("100", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethodVersion());
    }

    @Test
    public void normalize_removedConformanceMethodAllowedForCriterion_setsIdAndRemovalDateFields() {
        List<CertificationResultConformanceMethod> cms = new ArrayList<CertificationResultConformanceMethod>();
        cms.add(CertificationResultConformanceMethod.builder()
                .id(null)
                .conformanceMethod(ConformanceMethod.builder()
                        .id(null)
                        .name("NCQA eCQM Test Method")
                        .build())
                .conformanceMethodVersion("100")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(10L)
                                .number("170.315 (c)(2)")
                                .build())
                        .conformanceMethods(cms)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(5L, listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getId());
        assertEquals(LocalDate.parse("2022-06-01"), listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getRemovalDate());
        assertEquals("NCQA eCQM Test Method", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getName());
        assertEquals("100", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethodVersion());
    }

    @Test
    public void normalize_criterionHasAllowedConformanceMethods_addsAllowedConformanceMethodsToCertificationResult() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                    .id(1L)
                                    .number("170.315 (a)(1)")
                                    .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(2, listing.getCertificationResults().get(0).getAllowedConformanceMethods().size());
    }

    @Test
    public void normalize_criterionHasNoAllowedConformanceMethods_noAllowedConformanceMethodsAdded() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                    .id(4L)
                                    .number("170.315 (c)(2)")
                                    .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getAllowedConformanceMethods().size());
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
