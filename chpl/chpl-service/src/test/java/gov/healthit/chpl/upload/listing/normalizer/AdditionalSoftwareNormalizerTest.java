package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.CertifiedProductUtil;

public class AdditionalSoftwareNormalizerTest {
    private CertifiedProductUtil certifiedProductUtil;
    private AdditionalSoftwareNormalizer normalizer;

    @Before
    public void before() {
        certifiedProductUtil = Mockito.mock(CertifiedProductUtil.class);
        normalizer = new AdditionalSoftwareNormalizer(certifiedProductUtil);
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

        Mockito.when(certifiedProductUtil.getListing(ArgumentMatchers.anyString()))
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

        Mockito.when(certifiedProductUtil.getListing(ArgumentMatchers.anyString()))
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

        Mockito.when(certifiedProductUtil.getListing(ArgumentMatchers.anyString()))
            .thenReturn(CertifiedProduct.builder()
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

        Mockito.when(certifiedProductUtil.getListing(ArgumentMatchers.anyString()))
            .thenReturn(CertifiedProduct.builder()
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
