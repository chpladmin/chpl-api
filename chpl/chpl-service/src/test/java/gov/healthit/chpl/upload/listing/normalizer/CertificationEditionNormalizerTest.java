package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class CertificationEditionNormalizerTest {

    private CertificationEditionDAO editionDao;
    private CertificationEditionNormalizer normalizer;

    @Before
    public void setup() {
        editionDao = Mockito.mock(CertificationEditionDAO.class);
        normalizer = new CertificationEditionNormalizer(editionDao, new ValidationUtils(), new ChplProductNumberUtil());
    }

    @Test
    public void normalize_nullEdition_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getCertificationEdition());
    }

    @Test
    public void normalize_editionIdAndYearExist_normalChplProductNumber_noChanges() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 1L);
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .certificationEdition(edition)
                .build();
        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_editionIdAndYearExist_legacyChplProductNumber_noChanges() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 2L);
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2014");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .certificationEdition(edition)
                .build();
        normalizer.normalize(listing);

        assertEquals(2L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2014", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_yearKeyMissingEditionFoundById_yearAdded() throws EntityRetrievalException {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 1L);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(CertificationEdition.builder()
                    .id(1L)
                    .name("2015")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_yearKeyNullEditionFoundById_yearAdded() throws EntityRetrievalException {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 1L);
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(CertificationEdition.builder()
                    .id(1L)
                    .name("2015")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_yearKeyEmptyEditionFoundById_yearAdded() throws EntityRetrievalException {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 1L);
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(CertificationEdition.builder()
                    .id(1L)
                    .name("2015")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_yearKeyMissingEditionNotFoundById_yearNull() throws EntityRetrievalException {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 1L);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getById(ArgumentMatchers.anyLong()))
            .thenThrow(EntityRetrievalException.class);

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertNull(MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_idKeyMissingEditionFoundByYear_idAdded() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
            .thenReturn(CertificationEdition.builder()
                    .id(1L)
                    .name("2015")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_idKeyNullEditionFoundByYear_idAdded() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, null);
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
            .thenReturn(CertificationEdition.builder()
                    .id(1L)
                    .name("2015")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_idKeyEmptyEditionFoundByYear_idAdded() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, "");
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
            .thenReturn(CertificationEdition.builder()
                    .id(1L)
                    .name("2015")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_idKeyNaNEditionFoundByYear_idAdded() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, "this is not a number");
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
            .thenReturn(CertificationEdition.builder()
                    .id(1L)
                    .name("2015")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_idKeyMissingEditionNotFoundByYear_idNull() throws EntityRetrievalException {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2021");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(edition)
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
        .thenReturn(null);

        normalizer.normalize(listing);

        assertNull(MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2021", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_editionMissingEditionCodeInChplProductNumber_editionFound() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .build();

        Mockito.when(editionDao.getByYear(ArgumentMatchers.anyString()))
        .thenReturn(CertificationEdition.builder()
                .id(1L)
                .name("2015")
                .build());

        normalizer.normalize(listing);
        assertEquals(1L, MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY));
        assertEquals("2015", MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY));
    }

    @Test
    public void normalize_editionMissingEditionCodeMissingInChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(".07.07.2663.ABCD.R2.01.0.200511")
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getCertificationEdition());
    }

    @Test
    public void normalize_editionMissingEditionCodeInvalidInChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("??.07.07.2663.ABCD.R2.01.0.200511")
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getCertificationEdition());
    }

    @Test
    public void normalize_editionMissingLegacyChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getCertificationEdition());
    }
}
