package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ff4j.FF4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.fuzzyMatching.FuzzyChoicesManager;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.sed.CertifiedProductSed;
import gov.healthit.chpl.ucdProcess.UcdProcess;
import gov.healthit.chpl.ucdProcess.UcdProcessDAO;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UcdProcessNormalizerTest {

    private UcdProcessDAO ucdProcessDao;
    private FuzzyChoicesManager fuzzyChoicesManager;
    private UcdProcessNormalizer normalizer;
    private FF4j ff4j;

    @BeforeEach
    public void setup() {
        ucdProcessDao = Mockito.mock(UcdProcessDAO.class);
        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.eq(CertifiedProductUcdProcess.CUSTOM_UCD_PROCESS_ID)))
            .thenReturn(UcdProcess.builder().id(CertifiedProductUcdProcess.CUSTOM_UCD_PROCESS_ID).name("Custom").build());
        fuzzyChoicesManager = Mockito.mock(FuzzyChoicesManager.class);
        ff4j = Mockito.mock(FF4j.class);

        normalizer = new UcdProcessNormalizer(ucdProcessDao,
                fuzzyChoicesManager,
                Mockito.mock(ErrorMessageUtil.class),
                ff4j);
    }

    @Test
    public void normalize_nullSed_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setSed(null);
        normalizer.normalize(listing);
        assertNull(listing.getSed());
    }

    @Test
    public void normalize_nullUcdProcesses_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        listing.getSed().setUcdProcesses(null);
        normalizer.normalize(listing);
        assertNotNull(listing.getSed());
        assertNull(listing.getSed().getUcdProcesses());
    }

    @Test
    public void normalize_emptyUcdProcesses_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getSed());
        assertNotNull(listing.getSed().getUcdProcesses());
        assertEquals(0, listing.getSed().getUcdProcesses().size());
    }

    @Test
    public void normalize_ucdProcessNameFound_fillsInId() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResults(Stream.of(
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(1L).build()).build())
                        .collect(Collectors.toList()))
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .details("details")
                                .criteria(Stream.of(CertificationCriterion.builder().id(1L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
        Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.anyString()))
            .thenReturn(UcdProcess.builder()
                    .id(1L)
                    .name("ucd 1")
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getSed().getUcdProcesses().size());
        assertEquals(1, listing.getSed().getUcdProcesses().get(0).getId().longValue());
    }

    @Test
    public void normalize_ucdProcessNameNotFoundAndFuzzyMatchFound_setsValues() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResults(Stream.of(
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(1L).build()).build())
                        .collect(Collectors.toList()))
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .details("details")
                                .criteria(Stream.of(CertificationCriterion.builder().id(1L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
        Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.eq("ucd 1")))
            .thenReturn(null);
        Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.eq("ucd1")))
            .thenReturn(UcdProcess.builder()
                    .id(4L)
                    .name("ucd1")
                    .build());
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("ucd 1"), ArgumentMatchers.eq(FuzzyType.UCD_PROCESS)))
            .thenReturn("ucd1");

        normalizer.normalize(listing);
        assertEquals(1, listing.getSed().getUcdProcesses().size());
        assertEquals("ucd 1", listing.getSed().getUcdProcesses().get(0).getUserEnteredName());
        assertEquals("ucd1", listing.getSed().getUcdProcesses().get(0).getName());
        assertEquals(4L, listing.getSed().getUcdProcesses().get(0).getId());
    }

    @Test
    public void normalize_ucdProcessNameNotFoundAndFuzzyMatchNotFoundBeforeHti5_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResults(Stream.of(
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(1L).build()).build())
                        .collect(Collectors.toList()))
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .details("details")
                                .criteria(Stream.of(CertificationCriterion.builder().id(1L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.HTI_5_ERD))).thenReturn(false);
        Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.eq("ucd 1")))
            .thenReturn(null);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("ucd 1"), ArgumentMatchers.eq(FuzzyType.UCD_PROCESS)))
            .thenReturn(null);

        normalizer.normalize(listing);
        assertEquals(1, listing.getSed().getUcdProcesses().size());
        assertNull(listing.getSed().getUcdProcesses().get(0).getId());
        assertEquals("ucd 1", listing.getSed().getUcdProcesses().get(0).getName());
        assertNull(listing.getSed().getUcdProcesses().get(0).getUserEnteredName());
    }

    @Test
    public void normalize_ucdProcessNameNotFoundAndFuzzyMatchNotFoundAfterHti5_hasNullId() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResults(Stream.of(
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(1L).build()).build())
                        .collect(Collectors.toList()))
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .details("details")
                                .criteria(Stream.of(CertificationCriterion.builder().id(1L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.HTI_5_ERD))).thenReturn(true);
        Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.eq("ucd 1")))
            .thenReturn(null);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("ucd 1"), ArgumentMatchers.eq(FuzzyType.UCD_PROCESS)))
            .thenReturn(null);

        normalizer.normalize(listing);
        assertEquals(1, listing.getSed().getUcdProcesses().size());
        assertNull(listing.getSed().getUcdProcesses().get(0).getId());
        assertEquals("ucd 1", listing.getSed().getUcdProcesses().get(0).getName());
        assertNull(listing.getSed().getUcdProcesses().get(0).getUserEnteredName());
    }

    @Test
    public void normalize_ucdProcessNullNameHasDetailsAfterHti5_hasCustomId() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResults(Stream.of(
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(1L).build()).build())
                        .collect(Collectors.toList()))
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name(null)
                                .details("details")
                                .criteria(Stream.of(CertificationCriterion.builder().id(1L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.HTI_5_ERD))).thenReturn(true);

        normalizer.normalize(listing);
        assertEquals(1, listing.getSed().getUcdProcesses().size());
        assertEquals(CertifiedProductUcdProcess.CUSTOM_UCD_PROCESS_ID, listing.getSed().getUcdProcesses().get(0).getId());
        assertEquals("details", listing.getSed().getUcdProcesses().get(0).getDetails());
        assertEquals("Custom", listing.getSed().getUcdProcesses().get(0).getName());
        assertNull(listing.getSed().getUcdProcesses().get(0).getUserEnteredName());
    }

    @Test
    public void normalize_ucdProcessGroupingFromUploadFile_groupsCorrectly() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResults(Stream.of(
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(1L).build()).build(),
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(2L).build()).build(),
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(3L).build()).build())
                        .collect(Collectors.toList()))
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .criteria(Stream.of(CertificationCriterion.builder().id(1L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build(),
                                CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .criteria(Stream.of(CertificationCriterion.builder().id(2L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build(),
                                CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .criteria(Stream.of(CertificationCriterion.builder().id(3L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();

        Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(UcdProcess.builder()
                .id(1L)
                .name("ucd 1")
                .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getSed().getUcdProcesses().size());
        assertEquals(1L, listing.getSed().getUcdProcesses().get(0).getId());
        assertEquals("ucd 1", listing.getSed().getUcdProcesses().get(0).getName());
        assertEquals(3, listing.getSed().getUcdProcesses().get(0).getCriteria().size());
    }

    @Test
    public void normalize_ucdProcessGroupingAlreadyGrouped_groupsCorrectly() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResults(Stream.of(
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(1L).build()).build(),
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(2L).build()).build(),
                        CertificationResult.builder().success(true).criterion(CertificationCriterion.builder().id(3L).build()).build())
                        .collect(Collectors.toList()))
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .criteria(Stream.of(
                                        CertificationCriterion.builder().id(1L).build(),
                                        CertificationCriterion.builder().id(2L).build(),
                                        CertificationCriterion.builder().id(3L).build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();

        Mockito.when(ucdProcessDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(UcdProcess.builder()
                .id(1L)
                .name("ucd 1")
                .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getSed().getUcdProcesses().size());
        assertEquals(1L, listing.getSed().getUcdProcesses().get(0).getId());
        assertEquals("ucd 1", listing.getSed().getUcdProcesses().get(0).getName());
        assertEquals(3, listing.getSed().getUcdProcesses().get(0).getCriteria().size());
    }


    @Test
    public void normalize_ucdProcessWithCriteriaButNoOtherFields_ucdProcessIsRemoved() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .criteria(Stream.of(CertificationCriterion.builder()
                                        .id(1L)
                                        .number("170.315 (a)(1)")
                                        .build()).collect(Collectors.toCollection(LinkedHashSet::new)))
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(0, listing.getSed().getUcdProcesses().size());
    }
}
