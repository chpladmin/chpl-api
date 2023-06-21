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

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class CertificationBodyNormalizerTest {

    private CertificationBodyDAO acbDao;
    private CertificationBodyNormalizer normalizer;

    @Before
    public void setup() {
        acbDao = Mockito.mock(CertificationBodyDAO.class);
        normalizer = new CertificationBodyNormalizer(acbDao, new ChplProductNumberUtil(), new ValidationUtils());
    }

    @Test
    public void normalize_nullAcb_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getCertifyingBody());
    }

    @Test
    public void normalize_acbIdYearCodeExist_noChanges() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, 1L);
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Drummond");
        acb.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .certifyingBody(acb)
                .build();
        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_acbIdYearCodeExistLegacyChplProductNumber_noChanges() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, 1L);
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Drummond");
        acb.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .certifyingBody(acb)
                .build();
        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_nameKeyMissingAcbFoundById_nameAndCodeAdded() throws EntityRetrievalException {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, 1L);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(CertificationBody.builder()
                    .id(1L)
                    .name("Drummond")
                    .acbCode("04")
                    .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_nameKeyNullAcbFoundById_nameAndCodeAdded() throws EntityRetrievalException {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, 1L);
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Drummond")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_nameKeyEmptyAcbFoundById_nameAndCodeAdded() throws EntityRetrievalException {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, 1L);
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();
        Mockito.when(acbDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Drummond")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_nameKeyMissingAcbNotFoundById_nameAndCodeNull() throws EntityRetrievalException {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, 1L);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getById(ArgumentMatchers.anyLong()))
            .thenThrow(EntityRetrievalException.class);

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertNull(MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertNull(MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_idKeyMissingAcbFoundByCode_idAndNameAdded() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();
        Mockito.when(acbDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Drummond")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_idKeyMissingAcbFoundByName_idAdded() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Drummond");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Drummond")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);
        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_idKeyNullAcbFoundByName_idAndCodeAdded() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, null);
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Drummond");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Drummond")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);
        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_idKeyEmptyAcbFoundByName_idAndCodeAdded() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, "");
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Drummond");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Drummond")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);
        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_idKeyNaNAcbFoundByName_idAndCodeAdded() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, "this is not a number");
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Drummond");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Drummond")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);
        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Drummond", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_idKeyMissingAcbNotFoundByName_idAndCodeNull() throws EntityRetrievalException {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Unknown");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(null);

        normalizer.normalize(listing);

        assertNull(MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Unknown", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertNull(MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_idKeyMissingAcbNotFoundByCode_idAndNameNull() throws EntityRetrievalException {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "Unknown");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acb)
                .build();

        Mockito.when(acbDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(null);

        normalizer.normalize(listing);

        assertNull(MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertNull(MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("Unknown", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_acbMissingAcbCodeInChplProductNumber_acbFound() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .build();

        Mockito.when(acbDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(CertificationBody.builder()
                .id(1L)
                .name("Test")
                .acbCode("04")
                .build());

        normalizer.normalize(listing);

        assertEquals(1L, MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
        assertEquals("Test", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY));
        assertEquals("04", MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY));
    }

    @Test
    public void normalize_acbMissingAcbCodeMissingInChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07..2663.ABCD.R2.01.0.200511")
                .build();

        normalizer.normalize(listing);

        assertNull(listing.getCertifyingBody());
    }

    @Test
    public void normalize_acbInvalidAcbCodeMissingInChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.?M.2663.ABCD.R2.01.0.200511")
                .build();

        normalizer.normalize(listing);

        assertNull(listing.getCertifyingBody());
    }

    @Test
    public void normalize_acbMissingLegacyChplProductNumber_noLookup() throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();

        normalizer.normalize(listing);

        assertNull(listing.getCertifyingBody());
    }
}
