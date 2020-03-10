package gov.healthit.chpl.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class CertificationIdDaoTest {

    @Test
    public void findAll_WithData_ReturnsObjects() throws EntityCreationException, EntityRetrievalException {
        CertificationIdDAO ehrDao = Mockito.mock(CertificationIdDAO.class);
        Mockito.when(ehrDao.findAll()).thenReturn(getBasicCertIds());
        List<Long> ids = new ArrayList<Long>();
        ids.add(1L);
        ehrDao.create(ids, "2014");

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
