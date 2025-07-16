package gov.healthit.chpl.certificationId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.EntityManager;

public class CertificationIdDaoTest {

    private CertificationIdDAO certIdDao;

    @Before
    public void setup() {
        EntityManager entityManager = Mockito.mock(EntityManager.class);
        CertificationIdYearCalculator certIdYearCalculator = Mockito.mock(CertificationIdYearCalculator.class);
        Mockito.when(certIdYearCalculator.getInitialCmsIdTransitionToAnnualFormatDay()).thenReturn(LocalDate.now().plusDays(1));
        certIdDao = new CertificationIdDAO(certIdYearCalculator);
        certIdDao.setEntityManager(entityManager);
    }

    @Test
    public void create_2015CertificationId_generates15CString() throws EntityCreationException, EntityRetrievalException {
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(1L);
        CertificationIdDTO certId = certIdDao.create(listingIds, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void create_allCuresListings_generates15CString() throws EntityCreationException, EntityRetrievalException {
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(1L);
        listingIds.add(2L);
        CertificationIdDTO certId = certIdDao.create(listingIds, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void create_curesAndNotCuresListings_generates15CString() throws EntityCreationException, EntityRetrievalException {
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(1L);
        listingIds.add(2L);
        CertificationIdDTO certId = certIdDao.create(listingIds, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void create_curesCertificationId_generates15CString() throws EntityCreationException, EntityRetrievalException {
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(2L);
        CertificationIdDTO certId = certIdDao.create(listingIds, "2015");

        assertNotNull(certId);
        assertTrue(certId.getCertificationId().startsWith("0015C"));
    }

    @Test
    public void findAll_WithData_ReturnsObjects() throws EntityCreationException, EntityRetrievalException {
        CertificationIdDAO ehrDao = Mockito.mock(CertificationIdDAO.class);
        Mockito.when(ehrDao.findAll()).thenReturn(getBasicCertIds());
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(1L);
        ehrDao.create(listingIds, "2014");

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
