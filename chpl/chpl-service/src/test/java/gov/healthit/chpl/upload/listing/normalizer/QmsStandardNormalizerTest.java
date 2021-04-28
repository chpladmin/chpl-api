package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.manager.FuzzyChoicesManager;

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
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .qmsStandardName("test")
                        .build())
                .build();
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.anyString()))
            .thenReturn(QmsStandardDTO.builder()
                    .id(1L)
                    .name("test")
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getQmsStandards().size());
        assertEquals(1, listing.getQmsStandards().get(0).getQmsStandardId().longValue());
    }

    @Test
    public void normalize_qmsStandardNameNotFoundAndFuzzyMatchFound_setsValues() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .qmsStandardName("tst")
                        .build())
                .build();
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.eq("tst")))
            .thenReturn(null);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("tst"), ArgumentMatchers.eq(FuzzyType.QMS_STANDARD)))
            .thenReturn("test");

        normalizer.normalize(listing);
        assertEquals(1, listing.getQmsStandards().size());
        assertNull(listing.getQmsStandards().get(0).getQmsStandardId());
        assertEquals("tst", listing.getQmsStandards().get(0).getUserEnteredQmsStandardName());
        assertEquals("test", listing.getQmsStandards().get(0).getQmsStandardName());
    }

    @Test
    public void normalize_qmsStandardNameNotFoundAndFuzzyMatchNotFound_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .qmsStandardName("tst")
                        .build())
                .build();
        Mockito.when(qmsStandardDao.getByName(ArgumentMatchers.eq("tst")))
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
