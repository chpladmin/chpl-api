package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureDAO;
import gov.healthit.chpl.service.CertificationCriterionService;

public class MeasureNormalizerTest {
    private MeasureDAO measureDao;
    private ListingMeasureDAO listingMeasureDao;
    private CertificationCriterionService criteriaService;
    private MeasureNormalizer normalizer;

    private MeasureType g1, g2;

    @Before
    public void before() {
        measureDao = Mockito.mock(MeasureDAO.class);
        listingMeasureDao = Mockito.mock(ListingMeasureDAO.class);
        criteriaService = Mockito.mock(CertificationCriterionService.class);

        g1 = buildMeasureType(1L, "G1");
        g2 = buildMeasureType(2L, "G2");
        Mockito.when(listingMeasureDao.getMeasureTypes()).thenReturn(Stream.of(g1, g2).collect(Collectors.toSet()));

        normalizer = new MeasureNormalizer(measureDao, listingMeasureDao, criteriaService);
        normalizer.initialize();
    }

    @Test
    public void normalize_nullMeasures_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setMeasures(null);
        normalizer.normalize(listing);
        assertNull(listing.getMeasures());
    }

    @Test
    public void normalize_emptyMeasures_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .measures(new ArrayList<ListingMeasure>())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getMeasures().size());
    }

    @Test
    public void normalize_measureTypeg1_changedToG1AndHasId() {
        CertifiedProductSearchDetails listing =
                CertifiedProductSearchDetails.builder()
                    .measures(Stream.of(
                        ListingMeasure.builder()
                            .measureType(
                                MeasureType.builder()
                                .name("g1")
                                .build())
                        .build()
                        ).toList())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getMeasures().size());
        assertNotNull(listing.getMeasures().get(0).getMeasureType().getId());
        assertEquals(1L, listing.getMeasures().get(0).getMeasureType().getId());
        assertEquals("G1", listing.getMeasures().get(0).getMeasureType().getName());
    }

    @Test
    public void normalize_measureTypeG2_FoundAndHasId() {
        CertifiedProductSearchDetails listing =
                CertifiedProductSearchDetails.builder()
                    .measures(Stream.of(
                        ListingMeasure.builder()
                            .measureType(
                                MeasureType.builder()
                                .name("G2")
                                .build())
                        .build()
                        ).toList())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getMeasures().size());
        assertNotNull(listing.getMeasures().get(0).getMeasureType().getId());
        assertEquals(2L, listing.getMeasures().get(0).getMeasureType().getId());
        assertEquals("G2", listing.getMeasures().get(0).getMeasureType().getName());
    }

    @Test
    public void normalize_measureTypeg8_NotFoundAndNotChanged() {
        CertifiedProductSearchDetails listing =
                CertifiedProductSearchDetails.builder()
                    .measures(Stream.of(
                        ListingMeasure.builder()
                            .measureType(
                                MeasureType.builder()
                                .name("g8")
                                .build())
                        .build()
                        ).toList())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getMeasures().size());
        assertNull(listing.getMeasures().get(0).getMeasureType().getId());
        assertEquals("g8", listing.getMeasures().get(0).getMeasureType().getName());
    }

    private MeasureType buildMeasureType(Long id, String name) {
        return MeasureType.builder()
                .id(id)
                .name(name)
                .build();
    }
}
