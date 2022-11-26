package gov.healthit.chpl.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class CertificationIdDaoTest {

    private CertificationIdDAO certIdDao;
    private FF4j ff4j;

    @Before
    public void setup() {
        ff4j = Mockito.mock(FF4j.class);
        EntityManager entityManager = Mockito.mock(EntityManager.class);
        certIdDao = new CertificationIdDAO(ff4j);
        certIdDao.setEntityManager(entityManager);
    }

    @Test
    public void create_2014CertificationId_generates14EString() throws EntityCreationException, EntityRetrievalException {
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CAN_GENERATE_15C))).thenReturn(false);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CANNOT_GENERATE_15E))).thenReturn(false);

        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(1L)
                .year("2014")
                .build());
        CertificationIdDTO certId = certIdDao.create(listings, "2014");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0014E"));
    }

    @Test
    public void create_hybridCertificationId_generates15EString() throws EntityCreationException, EntityRetrievalException {
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CAN_GENERATE_15C))).thenReturn(false);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CANNOT_GENERATE_15E))).thenReturn(false);

        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(1L)
                .year("2014")
                .build());
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(2L)
                .year("2015")
                .build());
        CertificationIdDTO certId = certIdDao.create(listings, "2014/2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015H"));
    }

    @Test
    public void create_curesCertificationId_generates15CString() throws EntityCreationException, EntityRetrievalException {
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CAN_GENERATE_15C))).thenReturn(true);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.CANNOT_GENERATE_15E))).thenReturn(true);

        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(2L)
                .year("2015")
                .build());
        CertificationIdDTO certId = certIdDao.create(listings, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void findAll_WithData_ReturnsObjects() throws EntityCreationException, EntityRetrievalException {
        CertificationIdDAO ehrDao = Mockito.mock(CertificationIdDAO.class);
        Mockito.when(ehrDao.findAll()).thenReturn(getBasicCertIds());
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(1L)
                .year("2014")
                .build());
        ehrDao.create(listings, "2014");

        List<CertificationIdDTO> results = ehrDao.findAll();
        assertNotNull(results);
        assertEquals(1, results.size());
        CertificationIdDTO result = results.get(0);
        assertEquals("CertificationId", result.getCertificationId());
        assertEquals("key", result.getKey());
        assertEquals("2015", result.getYear());
    }

    private List<CertificationIdDTO> getBasicCertIds() {
        CertificationIdDTO dto = CertificationIdDTO.builder()
                .id(1L)
                .certificationId("CertificationId")
                .key("key")
                .year("2015")
                .build();
        List<CertificationIdDTO> ret = new ArrayList<CertificationIdDTO>();
        ret.add(dto);
        return ret;
    }
}
