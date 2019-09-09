package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import gov.healthit.chpl.changerequest.builders.CertificationBodyBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestCertificationBodyMapBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.changerequest.manager.ChangeRequestCertificationBodyMapHelper;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.exception.EntityRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class ChangeRequestCertificationBodyMapHelperTest {

    @Mock
    private ChangeRequestCertificationBodyMapDAO crCertificationBodyMapDAO;

    @InjectMocks
    private ChangeRequestCertificationBodyMapHelper crAcbMapHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCertificationBodiesByChangeRequestId_Single() {
        Mockito.when(crCertificationBodyMapDAO.getByChangeRequestId(ArgumentMatchers.anyLong()))
                .thenReturn(Arrays.asList(new ChangeRequestCertificationBodyMapBuilder()
                        .withId(23l)
                        .withCertificationBody(new CertificationBodyBuilder()
                                .withId(1l)
                                .withCode("0001")
                                .withName("ACB 1")
                                .build())
                        .withChangeRequest(new ChangeRequestBuilder()
                                .withId(1l)
                                .build())
                        .build()));

        List<CertificationBody> acbs = crAcbMapHelper.getCertificationBodiesByChangeRequestId(1l);

        assertNotNull(acbs);
        assertEquals(Integer.valueOf(1), Integer.valueOf(acbs.size()));
    }

    public void getCertificationBodiesByChangeRequestId_Multiple() {
        Mockito.when(crCertificationBodyMapDAO.getByChangeRequestId(ArgumentMatchers.anyLong()))
                .thenReturn(Arrays.asList(new ChangeRequestCertificationBodyMapBuilder()
                        .withId(23l)
                        .withCertificationBody(new CertificationBodyBuilder()
                                .withId(1l)
                                .withCode("0001")
                                .withName("ACB 1")
                                .build())
                        .withChangeRequest(new ChangeRequestBuilder()
                                .withId(1l)
                                .build())
                        .build(),
                        new ChangeRequestCertificationBodyMapBuilder()
                                .withId(23l)
                                .withCertificationBody(new CertificationBodyBuilder()
                                        .withId(2l)
                                        .withCode("0002")
                                        .withName("ACB 2")
                                        .build())
                                .withChangeRequest(new ChangeRequestBuilder()
                                        .withId(1l)
                                        .build())
                                .build()));

        List<CertificationBody> acbs = crAcbMapHelper.getCertificationBodiesByChangeRequestId(1l);

        assertNotNull(acbs);
        assertEquals(Integer.valueOf(2), Integer.valueOf(acbs.size()));
    }

    public void saveCertificationBody() throws EntityRetrievalException {
        Mockito.when(crCertificationBodyMapDAO.create(ArgumentMatchers.any(ChangeRequestCertificationBodyMap.class)))
                .thenReturn(new ChangeRequestCertificationBodyMapBuilder()
                        .withId(23l)
                        .withCertificationBody(new CertificationBodyBuilder()
                                .withId(1l)
                                .withCode("0001")
                                .withName("ACB 1")
                                .build())
                        .withChangeRequest(new ChangeRequestBuilder()
                                .withId(1l)
                                .build())
                        .build());

        ChangeRequestCertificationBodyMap map = crAcbMapHelper.saveCertificationBody(
                new ChangeRequestBuilder().withId(1l).build(),
                new CertificationBodyBuilder()
                        .withId(1l)
                        .withCode("0001")
                        .withName("ACB 1")
                        .build());

        assertNotNull(map);
    }

}
