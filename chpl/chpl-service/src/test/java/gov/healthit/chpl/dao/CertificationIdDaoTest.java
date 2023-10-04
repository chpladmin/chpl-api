package gov.healthit.chpl.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class CertificationIdDaoTest {

    private CertificationIdDAO certIdDao;

    @Before
    public void setup() {
        EntityManager entityManager = Mockito.mock(EntityManager.class);
        certIdDao = new CertificationIdDAO();
        certIdDao.setEntityManager(entityManager);
    }

    @Test
    public void create_2015CertificationId_generates15CString() throws EntityCreationException, EntityRetrievalException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(1L)
                .year("2015")
                .build());
        CertificationIdDTO certId = certIdDao.create(listings, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void create_allCuresListings_generates15CString() throws EntityCreationException, EntityRetrievalException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(1L)
                .year("2015")
                .curesUpdate(true)
                .build());
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(2L)
                .year("2015")
                .curesUpdate(true)
                .build());
        CertificationIdDTO certId = certIdDao.create(listings, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void create_curesAndNotCuresListings_generates15CString() throws EntityCreationException, EntityRetrievalException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(1L)
                .year("2015")
                .curesUpdate(false)
                .build());
        listings.add(CertifiedProductDetailsDTO.builder()
                .id(2L)
                .year("2015")
                .curesUpdate(true)
                .build());
        CertificationIdDTO certId = certIdDao.create(listings, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void create_curesCertificationId_generates15CString() throws EntityCreationException, EntityRetrievalException {
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
        assertEquals("2015", result.getYear());
    }

    private List<CertificationIdDTO> getBasicCertIds() {
        CertificationIdDTO dto = CertificationIdDTO.builder()
                .id(1L)
                .certificationId("CertificationId")
                .year("2015")
                .build();
        List<CertificationIdDTO> ret = new ArrayList<CertificationIdDTO>();
        ret.add(dto);
        return ret;
    }
}
