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

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestStandardDTO;

public class TestStandardNormalizerTest {
    private TestStandardDAO testStandardDao;
    private TestStandardNormalizer normalizer;

    @Before
    public void before() {
        testStandardDao = Mockito.mock(TestStandardDAO.class);
        normalizer = new TestStandardNormalizer(testStandardDao);
    }

    @Test
    public void normalize_nullTestStandard_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestStandards(null);
        normalizer.normalize(listing);
    }

    @Test
    public void normalize_emptyTestStandard_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .testStandards(new ArrayList<CertificationResultTestStandard>())
                        .build())
                .build();
        normalizer.normalize(listing);
    }

    @Test
    public void normalize_testStandardNotInDatabase_idIsNull() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardId(null)
                .testStandardName("notindb")
                .build());

        Mockito.when(testStandardDao.getByNumberAndEdition(ArgumentMatchers.eq("notindb"), ArgumentMatchers.eq(3L)))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .testStandards(testStandards)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestStandards().size());
        assertNull(listing.getCertificationResults().get(0).getTestStandards().get(0).getTestStandardId());
    }

    @Test
    public void normalize_testStandardInDatabase_setsId() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardId(null)
                .testStandardName("valid")
                .build());

        Mockito.when(testStandardDao.getByNumberAndEdition(ArgumentMatchers.eq("valid"), ArgumentMatchers.eq(3L)))
            .thenReturn(TestStandardDTO.builder()
                    .id(1L)
                    .name("valid")
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .testStandards(testStandards)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestStandards().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getTestStandards().get(0).getTestStandardId());
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
