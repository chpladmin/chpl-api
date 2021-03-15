package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

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
                .testingLabId(1L)
                .testingLabName("ICSA")
                .testingLabCode("TL")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("TL", listing.getTestingLabs().get(0).getTestingLabCode());
    }

    @Test
    public void normalize_testingLabCodeMissing_lookupById() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLabId(1L)
                .testingLabName("ICSA")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(TestingLabDTO.builder()
                .id(1L)
                .name("ICSA")
                .testingLabCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLabCode());
    }

    @Test
    public void normalize_testingLabNameMissing_lookupById() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLabId(1L)
                .testingLabCode("01")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(TestingLabDTO.builder()
                .id(1L)
                .name("ICSA")
                .testingLabCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLabCode());
    }

    @Test
    public void normalize_testingLabCodeMissing_lookupByIdNotFound() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLabId(1L)
                .testingLabName("ICSA")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong()))
        .thenThrow(EntityRetrievalException.class);
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertNull(listing.getTestingLabs().get(0).getTestingLabCode());
    }

    @Test
    public void normalize_testingLabIdMissing_lookupByName() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLabId(null)
                .testingLabName("ICSA")
                .testingLabCode("01")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(TestingLabDTO.builder()
                .id(1L)
                .name("ICSA")
                .testingLabCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLabCode());
    }

    @Test
    public void normalize_testingLabIdMissing_lookupByNameNotFound() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLabId(null)
                .testingLabName("ICSA")
                .testingLabCode("01")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertNull(listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLabCode());
    }

    public void normalize_testingLabIdAndNameMissing_lookupByCode() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLabId(null)
                .testingLabName("")
                .testingLabCode("01")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(TestingLabDTO.builder()
                .id(1L)
                .name("ICSA")
                .testingLabCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLabCode());
    }

    public void normalize_testingLabIdAndNameMissing_lookupByCodeNotFound() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLabId(null)
                .testingLabName("")
                .testingLabCode("01")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertNull(listing.getTestingLabs().get(0).getTestingLabId());
        assertEquals("", listing.getTestingLabs().get(0).getTestingLabName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLabCode());
    }
}
