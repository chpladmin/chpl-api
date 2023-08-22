package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.ChplProductNumberDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class CertificationEditionNormalizerTest {

    private CertificationEditionDAO editionDao;
    private CertificationEditionNormalizer normalizer;

    private CertificationEdition edition2015, edition2014;

    @Before
    public void setup() {
        edition2015 = CertificationEdition.builder()
                .id(1L)
                .name("2015")
                .build();
        edition2014 = CertificationEdition.builder()
                .id(2L)
                .name("2014")
                .build();

        editionDao = Mockito.mock(CertificationEditionDAO.class);
        FF4j ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.EDITIONLESS))).thenReturn(false);
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil(
                Mockito.mock(CertifiedProductSearchResultDAO.class),
                Mockito.mock(ChplProductNumberDAO.class),
                ff4j);
        normalizer = new CertificationEditionNormalizer(editionDao, new ValidationUtils(), chplProductNumberUtil);
    }

    @Test
    public void normalize_nullEdition_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getEdition());
    }

    @Test
    public void normalize_editionIdAndYearExist_normalChplProductNumber_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .edition(edition2015)
                .build();
        normalizer.normalize(listing);

        assertEquals(1L, listing.getEdition().getId());
        assertEquals("2015", listing.getEdition().getName());
    }

    @Test
    public void normalize_editionIdAndYearExist_legacyChplProductNumber_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .edition(edition2014)
                .build();
        normalizer.normalize(listing);

        assertEquals(2L, listing.getEdition().getId());
        assertEquals("2014", listing.getEdition().getName());
    }

    @Test
    public void normalize_yearMissingEditionFoundById_yearAdded() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().id(1L).build())
                .build();

        Mockito.when(editionDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(edition2015);

        normalizer.normalize(listing);

        assertEquals(1L, listing.getEdition().getId());
        assertEquals("2015", listing.getEdition().getName());
    }

    @Test
    public void normalize_yearEmptyEditionFoundById_yearAdded() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().id(1L).name("").build())
                .build();

        Mockito.when(editionDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(edition2015);
        normalizer.normalize(listing);

        assertEquals(1L, listing.getEdition().getId());
        assertEquals("2015", listing.getEdition().getName());
    }

    @Test
    public void normalize_yearMissingEditionNotFoundById_yearNull() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().id(1L).build())
                .build();

        Mockito.when(editionDao.getById(ArgumentMatchers.anyLong()))
            .thenThrow(EntityRetrievalException.class);

        normalizer.normalize(listing);

        assertEquals(1L, listing.getEdition().getId());
        assertNull(listing.getEdition().getName());
    }

    @Test
    public void normalize_idMissingEditionFoundByYear_idAdded() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().name("2015").build())
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
            .thenReturn(edition2015);

        normalizer.normalize(listing);

        assertEquals(1L, listing.getEdition().getId());
        assertEquals("2015", listing.getEdition().getName());
    }

    @Test
    public void normalize_idMissingEditionNotFoundByYear_idNull() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().name("2021").build())
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
        .thenReturn(null);

        normalizer.normalize(listing);

        assertNull(listing.getEdition().getId());
        assertEquals("2021", listing.getEdition().getName());
    }

    @Test
    public void normalize_editionMissingEditionCodeInChplProductNumber_editionFound() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
        .thenReturn(edition2015);

        normalizer.normalize(listing);
        assertEquals(1L, listing.getEdition().getId());
        assertEquals("2015", listing.getEdition().getName());
    }

    @Test
    public void normalize_editionMissingEditionCodeMissingInChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(".07.07.2663.ABCD.R2.01.0.200511")
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getEdition());
    }

    @Test
    public void normalize_editionMissingEditionCodeInvalidInChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("??.07.07.2663.ABCD.R2.01.0.200511")
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getEdition());
    }

    @Test
    public void normalize_editionMissingLegacyChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getEdition());
    }

    @Test
    public void normalize_editionMissingNoEditionCodeInChplProductNumber_editionNull() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(ChplProductNumberUtil.EDITION_CODE_NONE + ".07.04.2663.ABCD.R2.01.0.200511")
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getEdition());
    }
}
