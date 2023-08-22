package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class FunctionalityTestedNormalizerTest {

    private static final String RESTRICTED_FUNCTIONALITIES_TESTED_JSON = "[{\"criterionId\":27, \"restrictedFunctionalitiesTested\": "
            + "[{\"functionalityTestedId\":56, \"allowedRoleNames\":[\"ROLE_ADMIN\",\"ROLE_ONC\"]}]}]";
    private static final Long CRITERIA_ID_WITH_RESTRICTIONS = 27L;
    private static final Long CRITERIA_ID_WITHOUT_RESTRICTIONS = 13L;
    private static final Long FUNCTIONALITY_TESTED_ID_WITH_RESTRICTIONS = 56L;
    private static final Long FUNCTIONALITY_TESTED_ID_WITHOUT_RESTRICTIONS = 52L;

    private FunctionalityTestedDAO functionalityTestedDao;
    private ResourcePermissions resourcePermissions;
    private FunctionalityTestedNormalizer normalizer;

    @Before
    public void before() {
        functionalityTestedDao = Mockito.mock(FunctionalityTestedDAO.class);
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        normalizer = new FunctionalityTestedNormalizer(functionalityTestedDao,
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

        Mockito.when(functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps())
            .thenReturn(new HashMap<Long, List<FunctionalityTested>>());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(create2015Edition())
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

        Map<Long, List<FunctionalityTested>> funcTestedMaps = new HashMap<Long, List<FunctionalityTested>>();
        funcTestedMaps.put(CRITERIA_ID_WITHOUT_RESTRICTIONS, Stream.of(FunctionalityTested.builder()
                .id(1L)
                .name("valid")
                .description("valid")
                .criteria(Stream.of(CertificationCriterion.builder()
                        .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                        .number("170.315 (a)(13)")
                        .build()).toList())
                .build()).toList());
        Mockito.when(functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps())
            .thenReturn(funcTestedMaps);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(create2015Edition())
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

    private CertificationEdition create2015Edition() {
        return CertificationEdition.builder()
                .id(3L)
                .name("2015")
                .build();
    }
}
