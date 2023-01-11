package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.fuzzyMatching.FuzzyChoicesManager;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.qmsStandard.QmsStandard;
import gov.healthit.chpl.qmsStandard.QmsStandardDAO;

public class QmsStandardNormalizerTest {

    private QmsStandardDAO qmsStandardDao;
    private FuzzyChoicesManager fuzzyChoicesManager;
    private QmsStandardNormalizer normalizer;

    @Before
    public void setup() {
        qmsStandardDao = Mockito.mock(QmsStandardDAO.class);
        fuzzyChoicesManager = Mockito.mock(FuzzyChoicesManager.class);
        normalizer = new QmsStandardNormalizer(qmsStandardDao, fuzzyChoicesManager);
    }

    @Test
    public void normalize_nullQmsStandards_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setQmsStandards(null);
        normalizer.normalize(listing);
        assertNull(listing.getQmsStandards());
    }

    @Test
    public void normalize_emptyQmsStandards_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(new ArrayList<CertifiedProductQmsStandard>())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getQmsStandards());
        assertEquals(0, listing.getQmsStandards().size());
    }

    @Test
    public void normalize_qmsStandardNameFound_fillsInId() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .qmsStandardName("test")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.anyString()))
            .thenReturn(QmsStandard.builder()
                    .id(1L)
                    .name("test")
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getQmsStandards().size());
        assertEquals(1, listing.getQmsStandards().get(0).getQmsStandardId().longValue());
    }

    @Test
    public void normalize_qmsStandardNameNotFoundAndFuzzyMatchFound_setsValues() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .qmsStandardName("tst")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.eq("tst")))
            .thenReturn(null);
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.eq("test")))
            .thenReturn(QmsStandard.builder()
                    .id(1L)
                    .name("test")
                    .build());
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("tst"), ArgumentMatchers.eq(FuzzyType.QMS_STANDARD)))
            .thenReturn("test");

        normalizer.normalize(listing);
        assertEquals(1, listing.getQmsStandards().size());
        assertEquals(1L, listing.getQmsStandards().get(0).getQmsStandardId());
        assertEquals("tst", listing.getQmsStandards().get(0).getUserEnteredQmsStandardName());
        assertEquals("test", listing.getQmsStandards().get(0).getQmsStandardName());
    }

    @Test
    public void normalize_qmsStandardNameNotFoundAndFuzzyMatchNotFound_noChanges() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .qmsStandardName("tst")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.eq("tst")))
            .thenReturn(null);
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.eq("test")))
            .thenReturn(null);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("tst"), ArgumentMatchers.eq(FuzzyType.QMS_STANDARD)))
            .thenReturn(null);

        normalizer.normalize(listing);
        assertEquals(1, listing.getQmsStandards().size());
        assertNull(listing.getQmsStandards().get(0).getQmsStandardId());
        assertEquals("tst", listing.getQmsStandards().get(0).getQmsStandardName());
        assertNull(listing.getQmsStandards().get(0).getUserEnteredQmsStandardName());
    }
}
