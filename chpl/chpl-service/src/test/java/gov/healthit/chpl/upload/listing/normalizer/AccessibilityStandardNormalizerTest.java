package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.manager.FuzzyChoicesManager;

public class AccessibilityStandardNormalizerTest {

    private AccessibilityStandardDAO accessibilityStandardDao;
    private FuzzyChoicesManager fuzzyChoicesManager;
    private AccessibilityStandardNormalizer normalizer;

    @Before
    public void setup() {
        accessibilityStandardDao = Mockito.mock(AccessibilityStandardDAO.class);
        fuzzyChoicesManager = Mockito.mock(FuzzyChoicesManager.class);
        normalizer = new AccessibilityStandardNormalizer(accessibilityStandardDao, fuzzyChoicesManager);
    }

    @Test
    public void normalize_nullAccessibilityStandards_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setAccessibilityStandards(null);
        normalizer.normalize(listing);
        assertNull(listing.getAccessibilityStandards());
    }

    @Test
    public void normalize_emptyAccessibilityStandards_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(new ArrayList<CertifiedProductAccessibilityStandard>())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getAccessibilityStandards());
        assertEquals(0, listing.getAccessibilityStandards().size());
    }

    @Test
    public void normalize_accessibilityStandardNameFound_fillsInId() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName("test")
                        .build())
                .build();
        Mockito.when(accessibilityStandardDao.getByName(ArgumentMatchers.anyString()))
            .thenReturn(AccessibilityStandardDTO.builder()
                    .id(1L)
                    .name("test")
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getAccessibilityStandards().size());
        assertEquals(1, listing.getAccessibilityStandards().get(0).getAccessibilityStandardId().longValue());
    }

    @Test
    public void normalize_accessibilityStandardNameNotFoundAndFuzzyMatchFound_setsValues() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName("tst")
                        .build())
                .build();
        Mockito.when(accessibilityStandardDao.getByName(ArgumentMatchers.eq("tst")))
            .thenReturn(null);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("tst"), ArgumentMatchers.eq(FuzzyType.ACCESSIBILITY_STANDARD)))
            .thenReturn("test");

        normalizer.normalize(listing);
        assertEquals(1, listing.getAccessibilityStandards().size());
        assertNull(listing.getAccessibilityStandards().get(0).getAccessibilityStandardId());
        assertEquals("tst", listing.getAccessibilityStandards().get(0).getUserEnteredAccessibilityStandardName());
        assertEquals("test", listing.getAccessibilityStandards().get(0).getAccessibilityStandardName());
    }

    @Test
    public void normalize_accessibilityStandardNameNotFoundAndFuzzyMatchNotFound_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName("tst")
                        .build())
                .build();
        Mockito.when(accessibilityStandardDao.getByName(ArgumentMatchers.eq("tst")))
            .thenReturn(null);
        Mockito.when(fuzzyChoicesManager.getTopFuzzyChoice(ArgumentMatchers.eq("tst"), ArgumentMatchers.eq(FuzzyType.ACCESSIBILITY_STANDARD)))
            .thenReturn(null);

        normalizer.normalize(listing);
        assertEquals(1, listing.getAccessibilityStandards().size());
        assertNull(listing.getAccessibilityStandards().get(0).getAccessibilityStandardId());
        assertEquals("tst", listing.getAccessibilityStandards().get(0).getAccessibilityStandardName());
        assertNull(listing.getAccessibilityStandards().get(0).getUserEnteredAccessibilityStandardName());
    }
}
