package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;

public class OptionalStandardNormalizerTest {
    private OptionalStandardDAO optionalStandardDao;
    private TestStandardDAO testStandardDao;
    private OptionalStandardNormalizer normalizer;

    @Before
    public void before() {
        optionalStandardDao = Mockito.mock(OptionalStandardDAO.class);
        try {
            Mockito.when(optionalStandardDao.getAllOptionalStandardCriteriaMap())
                .thenReturn(buildOptionalStandardCriteriaMaps());
        } catch (EntityRetrievalException ex) {
            fail("Could not intiialize optional standard criteria maps");
        }

        testStandardDao = Mockito.mock(TestStandardDAO.class);
        normalizer = new OptionalStandardNormalizer(optionalStandardDao, testStandardDao);
    }

    private List<OptionalStandardCriteriaMap> buildOptionalStandardCriteriaMaps() {
        List<OptionalStandardCriteriaMap> maps = new ArrayList<OptionalStandardCriteriaMap>();
        maps.add(OptionalStandardCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                .id(1L)
                .optionalStandard(OptionalStandard.builder()
                        .id(1L)
                        .citation("valid")
                        .build())
                .build());
        return maps;
    }

    @Test
    public void normalize_nullOptionalStandard_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .build();
        listing.getCertificationResults().get(0).setOptionalStandards(null);
        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getOptionalStandards());
    }

    @Test
    public void normalize_emptyOptionalStandard_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .optionalStandards(new ArrayList<CertificationResultOptionalStandard>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getOptionalStandards().size());
    }

    @Test
    public void normalize_optionalStandardAndTestStandardNotInDatabase_idIsNullAndNoTestStandardAdded() {
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(null)
                .citation("notindb")
                .build());

        Mockito.when(testStandardDao.getByNumberAndEdition(ArgumentMatchers.eq("notindb"), ArgumentMatchers.anyLong()))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getOptionalStandards().size());
        assertNull(listing.getCertificationResults().get(0).getOptionalStandards().get(0).getOptionalStandardId());
        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getTestStandards()));
    }

    @Test
    public void normalize_optionalStandardNotInDatabaseAndTestStandardPresent_idIsNullAndTestStandardAdded() {
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(null)
                .citation("teststandard")
                .build());

        Mockito.when(testStandardDao.getByNumberAndEdition(ArgumentMatchers.eq("teststandard"), ArgumentMatchers.anyLong()))
            .thenReturn(TestStandardDTO.builder()
                    .id(1L)
                    .name("teststandard")
                    .description("teststandard desc")
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getOptionalStandards().size());
        assertNull(listing.getCertificationResults().get(0).getOptionalStandards().get(0).getOptionalStandardId());
        assertEquals(1, listing.getCertificationResults().get(0).getTestStandards().size());
        assertNotNull(listing.getCertificationResults().get(0).getTestStandards().get(0).getTestStandardId());
    }

    @Test
    public void normalize_optionalStandardInDatabaseForDifferentCriteriaAndNotATestStandard_idIsNull() {
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(null)
                .citation("valid")
                .build());

        Mockito.when(testStandardDao.getByNumberAndEdition(ArgumentMatchers.eq("valid"), ArgumentMatchers.anyLong()))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .build())
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getOptionalStandards().size());
        assertNull(listing.getCertificationResults().get(0).getOptionalStandards().get(0).getOptionalStandardId());
        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getTestStandards()));
    }

    @Test
    public void normalize_optionalStandardInDatabaseForDifferentCriteriaAndIsATestStandard_idIsNullAndTestStandardAdded() {
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(null)
                .citation("valid")
                .build());

        Mockito.when(testStandardDao.getByNumberAndEdition(ArgumentMatchers.eq("valid"), ArgumentMatchers.anyLong()))
        .thenReturn(TestStandardDTO.builder()
                .id(1L)
                .name("valid")
                .description("valid desc")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .build())
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getOptionalStandards().size());
        assertNull(listing.getCertificationResults().get(0).getOptionalStandards().get(0).getOptionalStandardId());
        assertEquals(1, listing.getCertificationResults().get(0).getTestStandards().size());
        assertNotNull(listing.getCertificationResults().get(0).getTestStandards().get(0).getTestStandardId());
    }

    @Test
    public void normalize_optionalStandardInDatabase_setsIdAndTestStandardNotAdded() {
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(null)
                .citation("valid")
                .build());

        Mockito.when(optionalStandardDao.getByCitation(ArgumentMatchers.eq("valid")))
            .thenReturn(OptionalStandard.builder()
                    .id(1L)
                    .citation("valid")
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getOptionalStandards().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getOptionalStandards().get(0).getOptionalStandardId());
        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getTestStandards()));
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
