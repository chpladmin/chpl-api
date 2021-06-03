package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class AdditionalSoftwareNormalizerTest {
    private CertifiedProductDAO certifiedProductDao;
    private AdditionalSoftwareNormalizer normalizer;

    @Before
    public void before() {
        certifiedProductDao = Mockito.mock(CertifiedProductDAO.class);
        normalizer = new AdditionalSoftwareNormalizer(certifiedProductDao);
    }

    @Test
    public void normalize_nullAdditionalSoftware_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .build();
        listing.getCertificationResults().get(0).setAdditionalSoftware(null);
        normalizer.normalize(listing);

        assertNull(listing.getCertificationResults().get(0).getAdditionalSoftware());
    }

    @Test
    public void normalize_emptyAdditionalSoftware_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .additionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getAdditionalSoftware().size());
    }

    @Test
    public void normalize_additionalSoftwareWithoutListing_noChanges() {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .name("test")
                .version("1")
                .grouping("A")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getAdditionalSoftware().size());
        assertNull(listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductId());
        assertNull(listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductNumber());
        assertEquals("test", listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getName());
        assertEquals("1", listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getVersion());
        assertEquals("A", listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getGrouping());

    }

    @Test
    public void normalize_additionalSoftwareNewStyleChplProductNumberNotInDb_listingIdIsNull() throws EntityRetrievalException {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber("15.02.02.3007.A056.01.00.0.180214")
                .build());

        Mockito.when(certifiedProductDao.getByChplUniqueId(ArgumentMatchers.anyString()))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getAdditionalSoftware().size());
        assertNull(listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductId());
        assertEquals("15.02.02.3007.A056.01.00.0.180214",
                listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductNumber());
    }

    public void normalize_additionalSoftwareLegacyStyleChplProductNumberNotInDb_listingIdIsNull() throws EntityRetrievalException {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber("CHP-008408")
                .build());

        Mockito.when(certifiedProductDao.getByChplNumber(ArgumentMatchers.anyString()))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getAdditionalSoftware().size());
        assertNull(listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductId());
        assertEquals("CHP-008408",
                listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductNumber());
    }

    public void normalize_additionalSoftwareInvalidStyleChplProductNumber_listingIdIsNull() throws EntityRetrievalException {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber("junk")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getAdditionalSoftware().size());
        assertNull(listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductId());
        assertEquals("junk", listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductNumber());
    }

    @Test
    public void normalize_additionalSoftwareNewStyleChplProductNumberInDb_setsListingId() throws EntityRetrievalException {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber("15.02.02.3007.A056.01.00.0.180214")
                .build());

        Mockito.when(certifiedProductDao.getByChplUniqueId(ArgumentMatchers.anyString()))
            .thenReturn(CertifiedProductDetailsDTO.builder()
                    .id(1L)
                    .chplProductNumber("15.02.02.3007.A056.01.00.0.180214")
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getAdditionalSoftware().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductId());
        assertEquals("15.02.02.3007.A056.01.00.0.180214",
                listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductNumber());
    }

    @Test
    public void normalize_additionalSoftwareLegacyStyleChplProductNumberInDb_setsListingId() throws EntityRetrievalException {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber("CHP-008408")
                .build());

        Mockito.when(certifiedProductDao.getByChplNumber(ArgumentMatchers.anyString()))
            .thenReturn(CertifiedProductDTO.builder()
                    .id(1L)
                    .chplProductNumber("CHP-008408")
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getAdditionalSoftware().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductId());
        assertEquals("CHP-008408",
                listing.getCertificationResults().get(0).getAdditionalSoftware().get(0).getCertifiedProductNumber());
    }
}
