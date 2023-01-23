package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.fuzzyMatching.FuzzyChoicesManager;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.ucdProcess.UcdProcess;
import gov.healthit.chpl.ucdProcess.UcdProcessDAO;

public class UcdProcessNormalizerTest {

    private UcdProcessDAO ucdProcessDao;
    private FuzzyChoicesManager fuzzyChoicesManager;
    private UcdProcessNormalizer normalizer;

    @Before
    public void setup() {
        ucdProcessDao = Mockito.mock(UcdProcessDAO.class);
        fuzzyChoicesManager = Mockito.mock(FuzzyChoicesManager.class);
        normalizer = new UcdProcessNormalizer(ucdProcessDao, fuzzyChoicesManager);
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
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .details("details")
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
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .details("details")
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
    public void normalize_ucdProcessNameNotFoundAndFuzzyMatchNotFound_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .name("ucd 1")
                                .details("details")
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
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
    public void normalize_ucdProcessWithCriteriaButNoOtherFields_ucdProcessIsRemoved() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .ucdProcesses(Stream.of(CertifiedProductUcdProcess.builder()
                                .criteria(Stream.of(CertificationCriterion.builder()
                                        .id(1L)
                                        .number("170.315 (a)(1)")
                                        .build()).toList())
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(0, listing.getSed().getUcdProcesses().size());
    }
}
