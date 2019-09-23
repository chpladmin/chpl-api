package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.builders.CertificationBodyBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestTypeBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestWebsiteBuilder;
import gov.healthit.chpl.changerequest.builders.DeveloperBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestCertificationBodyHelper;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManagerImpl;
import gov.healthit.chpl.changerequest.manager.ChangeRequestStatusHelper;
import gov.healthit.chpl.changerequest.manager.ChangeRequestWebsiteHelper;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationFactory;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class ChangeRequestManagerImplTest {
    @Mock
    private ChangeRequestDAO changeRequestDAO;

    @Mock
    private ChangeRequestTypeDAO changeRequestTypeDAO;

    @Mock
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;

    @Mock
    private DeveloperDAO developerDAO;

    @Mock
    private CertifiedProductDAO certifiedProductDAO;

    @Mock
    private CertificationBodyDAO certificationBodyDAO;

    @Mock
    private ChangeRequestCertificationBodyHelper crCertificationBodyMapHelper;

    @Mock
    private ChangeRequestStatusHelper crStatusHelper;

    @Mock
    private ChangeRequestWebsiteHelper crWebsiteHelper;

    @Mock
    private ChangeRequestValidationFactory crValidationFactory;

    @InjectMocks
    private ChangeRequestManagerImpl changeRequestManager;

    private Long pendingAcbActionStatus = 1l;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(changeRequestManager, "websiteChangeRequestType", 1l);
        ReflectionTestUtils.setField(changeRequestManager, "pendingAcbActionStatus", 1l);
    }

    @Test
    public void getChangeRequest_Success() throws EntityRetrievalException {
        // Setup
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder().withId(20l).withCode("1234").withName("Dev 1").build())
                        .withChangeRequestType(
                                new ChangeRequestTypeBuilder().withId(1l).withName("Website Chnage Request").build())
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(8l)
                                .withComment("Comment")
                                .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .withName("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .addChangeRequestStatus(new ChangeRequestStatusBuilder()
                                .withId(8l)
                                .withComment("Comment")
                                .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .withName("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .addCertificationBody(new CertificationBodyBuilder()
                                .withId(1l)
                                .withCode("1234")
                                .withName("ACB 1234")
                                .build())
                        .build());

        Mockito.when(crWebsiteHelper.getByChangeRequestId(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestWebsiteBuilder().withId(5l).withWebsite("website.com").build());

        // Run
        ChangeRequest cr = changeRequestManager.getChangeRequest(1l);

        // Check
        assertNotNull(cr);
        assertEquals(1, cr.getStatuses().size());
        assertEquals(1, cr.getCertificationBodies().size());
        assertNotNull(cr.getDetails());
        assertNotNull(cr.getCurrentStatus());
    }
}
