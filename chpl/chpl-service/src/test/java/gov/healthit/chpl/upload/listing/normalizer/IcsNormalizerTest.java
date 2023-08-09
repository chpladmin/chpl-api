package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.util.CertifiedProductUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.DateUtil;

public class IcsNormalizerTest {

    private CertifiedProductUtil cpUtil;
    private IcsNormalizer normalizer;

    @Before
    public void setup() {
        cpUtil = Mockito.mock(CertifiedProductUtil.class);
        normalizer = new IcsNormalizer(cpUtil, new ChplProductNumberUtil());
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
    public void normalize_icsNullInListingBut0InChplProductNumber_setsIcsToFalse() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.07.2663.ABCD.R2.00.0.200511")
                .ics(null)
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getInherits());
        assertFalse(listing.getIcs().getInherits());
    }

    @Test
    public void normalize_icsBooleanNullInListingBut0InChplProductNumber_setsIcsToFalse() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.07.2663.ABCD.R2.00.0.200511")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(null)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getInherits());
        assertFalse(listing.getIcs().getInherits());
    }

    @Test
    public void normalize_icsBooleanNullInListingBut1InChplProductNumber_setsIcsToTrue() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.07.2663.ABCD.R2.01.0.200511")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(null)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getInherits());
        assertTrue(listing.getIcs().getInherits());
    }

    @Test
    public void normalize_icsBooleanNullInListingButLargeNumberInChplProductNumber_setsIcsToTrue() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.07.2663.ABCD.R2.98.0.200511")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(null)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getInherits());
        assertTrue(listing.getIcs().getInherits());
    }

    @Test
    public void normalize_icsNullInListingAndInvalidInChplProductNumber_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.07.2663.ABCD.R2.YY.200511")
                .ics(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getIcs());
    }

    @Test
    public void normalize_icsBooleanNullInListingAndInvalidInChplProductNumber_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.07.2663.ABCD.R2.YY.200511")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(null)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNull(listing.getIcs().getInherits());
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
        Mockito.when(cpUtil.getListing(ArgumentMatchers.anyString()))
        .thenReturn(CertifiedProduct.builder()
                .id(1L)
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2023-06-10")))
                .certificationStatus("Active")
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
        Mockito.when(cpUtil.getListing(ArgumentMatchers.anyString()))
            .thenReturn(null);
        normalizer.normalize(listing);
        assertNotNull(listing.getIcs());
        assertNotNull(listing.getIcs().getParents());
        assertEquals(1, listing.getIcs().getParents().size());
        assertNull(listing.getIcs().getParents().get(0).getId());
        assertEquals("15.04.04.2526.WEBe.06.00.1.210101", listing.getIcs().getParents().get(0).getChplProductNumber());
    }
}
