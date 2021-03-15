package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;

public class TestingLabNormalizerTest {

    private TestingLabDAO atlDao;
    private TestingLabNormalizer normalizer;

    @Before
    public void setup() {
        atlDao = Mockito.mock(TestingLabDAO.class);
        normalizer = new TestingLabNormalizer(atlDao);
    }

    @Test
    public void normalize_nullTestingLabs_noChanges() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setTestingLabs(null);
        normalizer.normalize(listing);
        assertNull(listing.getTestingLabs());
    }

    @Test
    public void normalize_emptyTestingLabs_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(new ArrayList<CertifiedProductTestingLab>())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getTestingLabs());
        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void normalize_testingLabIdNameCodeExist_noLookup() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .id(1L)
                .testingLabName("ICSA")
                .testingLabCode("TL")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("TL", listing.getTestingLabs().get(0).getTestingLabCode());
    }
}
