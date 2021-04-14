package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import javax.persistence.EntityNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;

public class IcsNormalizerTest {

    private CertifiedProductSearchDAO cpDao;
    private IcsNormalizer normalizer;

    @Before
    public void setup() {
        cpDao = Mockito.mock(CertifiedProductSearchDAO.class);
        normalizer = new IcsNormalizer(cpDao);
    }

    @Test
    public void normalize_nullIcs_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getIcs());
    }

    @Test
    public void normalize_nullIcsParents_noChanges() {
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setParents(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(ics)
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNull(listing.getIcs().getParents());
    }

    @Test
    public void normalize_emptyIcsParents_noChanges() {
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setParents(new ArrayList<CertifiedProduct>());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(ics)
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getParents());
        assertEquals(0, listing.getIcs().getParents().size());
    }

    @Test
    public void normalize_icsParentsHaveIds_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .parent(CertifiedProduct.builder()
                                .id(1L)
                                .build())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getParents());
        assertEquals(1, listing.getIcs().getParents().size());
        assertEquals(1L, listing.getIcs().getParents().get(0).getId());
    }

    @Test
    public void normalize_icsParentsMissingIds_getsData() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .parent(CertifiedProduct.builder()
                                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                                .build())
                        .build())
                .build();
        Mockito.when(cpDao.getByChplProductNumber(ArgumentMatchers.anyString()))
        .thenReturn(CertifiedProduct.builder()
                .id(1L)
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                 .build());
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getParents());
        assertEquals(1, listing.getIcs().getParents().size());
        assertEquals(1L, listing.getIcs().getParents().get(0).getId());
        assertEquals("15.04.04.2526.WEBe.06.00.1.210101", listing.getIcs().getParents().get(0).getChplProductNumber());
    }

    @Test
    public void normalize_icsParentsMissingIdsNoMatchingChplProductNumber_getsNoData() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .parent(CertifiedProduct.builder()
                                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                                .build())
                        .build())
                .build();
        Mockito.when(cpDao.getByChplProductNumber(ArgumentMatchers.anyString()))
        .thenThrow(EntityNotFoundException.class);
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getParents());
        assertEquals(1, listing.getIcs().getParents().size());
        assertNull(listing.getIcs().getParents().get(0).getId());
        assertEquals("15.04.04.2526.WEBe.06.00.1.210101", listing.getIcs().getParents().get(0).getChplProductNumber());
    }
}
