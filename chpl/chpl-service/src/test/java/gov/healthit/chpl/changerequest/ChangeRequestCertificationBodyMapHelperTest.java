package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.changerequest.builders.DeveloperBuilder;
import gov.healthit.chpl.changerequest.manager.ChangeRequestCertificationBodyHelper;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class ChangeRequestCertificationBodyMapHelperTest {

    @Mock
    private CertifiedProductDAO certifiedProductDAO;

    @Mock
    private CertificationBodyDAO certificationBodyDAO;

    @InjectMocks
    private ChangeRequestCertificationBodyHelper crAcbHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCertificationBodiesByDeveloper_Success() throws EntityRetrievalException {
        Mockito.when(certifiedProductDAO.findByDeveloperId(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProducts());

        Mockito.when(certificationBodyDAO.getById(13l))
                .thenReturn(getAcb(13l, "13", "ACB13"));

        List<CertificationBody> acbs = crAcbHelper.getCertificationBodiesByDeveloper(
                new DeveloperBuilder().withId(1l).build());

        assertEquals(1, acbs.size());
    }

    private List<CertifiedProductDetailsDTO> getCertifiedProducts() {
        CertifiedProductDetailsDTO cp1 = new CertifiedProductDetailsDTO();
        cp1.setId(1l);
        cp1.setCertificationBodyId(13l);

        CertifiedProductDetailsDTO cp2 = new CertifiedProductDetailsDTO();
        cp2.setId(2l);
        cp2.setCertificationBodyId(13l);

        return new ArrayList<CertifiedProductDetailsDTO>(Arrays.asList(cp1, cp2));
    }

    private CertificationBodyDTO getAcb(final Long id, final String code, final String name) {
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(id);
        acb.setAcbCode(code);
        acb.setName(name);
        return acb;
    }
}
