package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedManager;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class FunctionalityTestedNormalizerTest {

    private static final String RESTRICTED_FUNCTIONALITIES_TESTED_JSON = "[{\"criterionId\":27, \"restrictedFunctionalitiesTested\": "
            + "[{\"functionalityTestedId\":56, \"allowedRoleNames\":[\"ROLE_ADMIN\",\"ROLE_ONC\"]}]}]";
    private static final Long CRITERIA_ID_WITH_RESTRICTIONS = 27L;
    private static final Long CRITERIA_ID_WITHOUT_RESTRICTIONS = 13L;
    private static final Long FUNCTIONALITY_TESTED_ID_WITH_RESTRICTIONS = 56L;
    private static final Long FUNCTIONALITY_TESTED_ID_WITHOUT_RESTRICTIONS = 52L;

    private FunctionalityTestedDAO functionalityTestedDao;
    private FunctionalityTestedManager functionalityTestedManager;
    private ResourcePermissions resourcePermissions;
    private FunctionalityTestedNormalizer normalizer;

    @Before
    public void before() {
        functionalityTestedDao = Mockito.mock(FunctionalityTestedDAO.class);
        functionalityTestedManager = Mockito.mock(FunctionalityTestedManager.class);
        Mockito.when(functionalityTestedManager.getFunctionalitiesTested(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
            .thenReturn(new ArrayList<FunctionalityTested>());

        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        normalizer = new FunctionalityTestedNormalizer(functionalityTestedDao, functionalityTestedManager,
                resourcePermissions, RESTRICTED_FUNCTIONALITIES_TESTED_JSON);
    }

    @Test
    public void normalize_nullFunctionalitiesTested_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .build())
                .build();
        listing.getCertificationResults().get(0).setFunctionalitiesTested(null);
        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getFunctionalitiesTested());
    }

    @Test
    public void normalize_emptyFunctionalitiesTested_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
    }

    @Test
    public void normalize_functionalityTestedNotInDatabase_idIsNull() {
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(null)
                .name("notindb")
                .build());
        Map<String, Object> editionMap = create2015EditionMap();

        Mockito.when(functionalityTestedDao.getByNumberAndEdition(ArgumentMatchers.eq("notindb"), ArgumentMatchers.eq(3L)))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(editionMap)
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertNull(listing.getCertificationResults().get(0).getFunctionalitiesTested().get(0).getFunctionalityTestedId());
    }

    @Test
    public void normalize_functionalityTestedInDatabase_setsId() {
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(null)
                .name("valid")
                .build());
        Map<String, Object> editionMap = create2015EditionMap();

        Mockito.when(functionalityTestedDao.getByNumberAndEdition(ArgumentMatchers.eq("valid"), ArgumentMatchers.eq(3L)))
            .thenReturn(FunctionalityTested.builder()
                    .id(1L)
                    .name("valid")
                    .description("valid")
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(editionMap)
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getFunctionalitiesTested().get(0).getFunctionalityTestedId());
    }

    @Test
    public void normalize_noRestrictedFunctionalityTested_functionalityTestedNotRemoved() {
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_WITHOUT_RESTRICTIONS)
                .name("(a)(13)(iii)")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
    }

    @Test
    public void normalize_restrictedFunctionalityTestedAndUserHasValidRole_functionalityTestedNotRemoved() {
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.anyList()))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_WITH_RESTRICTIONS)
                .name("(c)(3)(ii)")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITH_RESTRICTIONS)
                                .number("170.315 (c)(3)")
                                .build())
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
    }

    @Test
    public void normalize_restrictedFunctionalityTestedAndUserDoesNotHaveValidRole_functionalityTestedRemoved() {
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.anyList()))
            .thenReturn(false);

        List<CertificationResultFunctionalityTested> functionalityTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalityTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_WITH_RESTRICTIONS)
                .name("(c)(3)(ii)")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITH_RESTRICTIONS)
                                .number("170.315 (c)(3)")
                                .build())
                        .functionalitiesTested(functionalityTested)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
    }

    @Test
    public void normalize_hasAllowedValues_addsAllowedFunctionalityTested() {
        List<FunctionalityTested> allowedFunctionalitiesTested = new ArrayList<FunctionalityTested>();
        allowedFunctionalitiesTested.add(FunctionalityTested.builder()
                .name("TF1")
                .description("tf1 desc")
                .id(1L)
                .build());
        allowedFunctionalitiesTested.add(FunctionalityTested.builder()
                .name("TF2")
                .description("tf2 desc")
                .id(2L)
                .build());
        Mockito.when(functionalityTestedManager.getFunctionalitiesTested(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.nullable(Long.class)))
            .thenReturn(allowedFunctionalitiesTested);

        Map<String, Object> editionMap = create2015EditionMap();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(editionMap)
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(2, listing.getCertificationResults().get(0).getAllowedTestFunctionalities().size());
        assertTrue(listing.getCertificationResults().get(0).getAllowedTestFunctionalities().stream()
                .map(tf -> tf.getId())
                .collect(Collectors.toList()).contains(1L));
        assertTrue(listing.getCertificationResults().get(0).getAllowedTestFunctionalities().stream()
                .map(tf -> tf.getId())
                .collect(Collectors.toList()).contains(2L));
    }

    @Test
    public void normalize_hasNoAllowedValues_addsNoAllowedFunctionalityTested() {
        List<FunctionalityTested> allowedFunctionalitiesTested = new ArrayList<FunctionalityTested>();
        allowedFunctionalitiesTested.add(FunctionalityTested.builder()
                .name("TF1")
                .description("tf1 desc")
                .id(1L)
                .build());
        allowedFunctionalitiesTested.add(FunctionalityTested.builder()
                .name("TF2")
                .description("tf2 desc")
                .id(2L)
                .build());
        Mockito.when(functionalityTestedManager.getFunctionalitiesTested(ArgumentMatchers.eq(4L), ArgumentMatchers.anyString(), ArgumentMatchers.nullable(Long.class)))
            .thenReturn(new ArrayList<FunctionalityTested>());

        Map<String, Object> editionMap = create2015EditionMap();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(editionMap)
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(0, listing.getCertificationResults().get(0).getAllowedTestFunctionalities().size());
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
