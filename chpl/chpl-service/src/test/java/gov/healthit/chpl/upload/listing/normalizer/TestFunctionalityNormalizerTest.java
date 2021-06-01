package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class TestFunctionalityNormalizerTest {

    private static final String RESTRICTED_TEST_FUNCTIONALITY_JSON = "[{\"criteriaId\":27, \"restrictedTestFunctionalities\": "
            + "[{\"testFunctionalityId\":56, \"allowedRoleNames\":[\"ROLE_ADMIN\",\"ROLE_ONC\"]}]}]";
    private static final Long CRITERIA_ID_WITH_RESTRICTIONS = 27L;
    private static final Long CRITERIA_ID_WITHOUT_RESTRICTIONS = 13L;
    private static final Long TEST_FUNCTIONALITY_ID_WITH_RESTRICTIONS = 56L;
    private static final Long TEST_FUNCTIONALITY_ID_WITHOUT_RESTRICTIONS = 52L;

    private TestFunctionalityDAO testFunctionalityDao;
    private ResourcePermissions resourcePermissions;
    private TestFunctionalityNormalizer normalizer;

    @Before
    public void before() {
        testFunctionalityDao = Mockito.mock(TestFunctionalityDAO.class);
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        normalizer = new TestFunctionalityNormalizer(testFunctionalityDao, resourcePermissions, RESTRICTED_TEST_FUNCTIONALITY_JSON);
    }

    @Test
    public void normalize_nullTestFunctionality_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestFunctionality(null);
        normalizer.normalize(listing);
    }

    @Test
    public void normalize_emptyTestFunctionality_noChanges() {
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
    }

    @Test
    public void normalize_testFunctionalityNotInDatabase_idIsNull() {
        List<CertificationResultTestFunctionality> testFunctionalities = new ArrayList<CertificationResultTestFunctionality>();
        testFunctionalities.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(null)
                .name("notindb")
                .build());
        Map<String, Object> editionMap = create2015EditionMap();

        Mockito.when(testFunctionalityDao.getByNumberAndEdition(ArgumentMatchers.eq("notindb"), ArgumentMatchers.eq(3L)))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(editionMap)
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .testFunctionality(testFunctionalities)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestFunctionality().size());
        assertNull(listing.getCertificationResults().get(0).getTestFunctionality().get(0).getTestFunctionalityId());
    }

    @Test
    public void normalize_testFunctionalityInDatabase_setsId() {
        List<CertificationResultTestFunctionality> testFunctionalities = new ArrayList<CertificationResultTestFunctionality>();
        testFunctionalities.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(null)
                .name("valid")
                .build());
        Map<String, Object> editionMap = create2015EditionMap();

        Mockito.when(testFunctionalityDao.getByNumberAndEdition(ArgumentMatchers.eq("valid"), ArgumentMatchers.eq(3L)))
            .thenReturn(TestFunctionalityDTO.builder()
                    .id(1L)
                    .name("valid")
                    .number("valid")
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(editionMap)
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .testFunctionality(testFunctionalities)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestFunctionality().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getTestFunctionality().get(0).getTestFunctionalityId());
    }

    @Test
    public void normalize_noRestrictedTestFunctionality_testFunctionalityNotRemoved() {
        List<CertificationResultTestFunctionality> testFunctionalities = new ArrayList<CertificationResultTestFunctionality>();
        testFunctionalities.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(TEST_FUNCTIONALITY_ID_WITHOUT_RESTRICTIONS)
                .name("(a)(13)(iii)")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .testFunctionality(testFunctionalities)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestFunctionality().size());
    }

    @Test
    public void normalize_restrictedTestFunctionalityAndUserHasValidRole_testFunctionalityNotRemoved() {
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.anyList()))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFunctionalities = new ArrayList<CertificationResultTestFunctionality>();
        testFunctionalities.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(TEST_FUNCTIONALITY_ID_WITH_RESTRICTIONS)
                .name("(c)(3)(ii)")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITH_RESTRICTIONS)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionality(testFunctionalities)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestFunctionality().size());
    }

    @Test
    public void normalize_restrictedTestFunctionalityAndUserDoesNotHaveValidRole_testFunctionalityRemoved() {
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.anyList()))
            .thenReturn(false);

        List<CertificationResultTestFunctionality> testFunctionalities = new ArrayList<CertificationResultTestFunctionality>();
        testFunctionalities.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(TEST_FUNCTIONALITY_ID_WITH_RESTRICTIONS)
                .name("(c)(3)(ii)")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CRITERIA_ID_WITH_RESTRICTIONS)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionality(testFunctionalities)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getTestFunctionality().size());
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
