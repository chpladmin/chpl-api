package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.manager.ChangeRequestStatusHelper;
import gov.healthit.chpl.exception.EntityRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class ChangeRequestStatusHelperTest {
    @Mock
    private ChangeRequestStatusDAO crStatusDAO;

    @Mock
    private ChangeRequestStatusTypeDAO crStatusTypeDAO;

    @InjectMocks
    private ChangeRequestStatusHelper changeRequestStatusHelper;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void saveInitialStatus_Success() throws EntityRetrievalException {
        // Setup
        Mockito.when(crStatusDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestStatus.class)))
                .thenReturn(new ChangeRequestStatusBuilder()
                        .withId(1l)
                        .withComment("")
                        .withStatusChangeDate(new Date())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(pendingAcbActionStatus)
                                .withName("Pending ONC-ACB Action")
                                .build())
                        .build());

        // Run
        ChangeRequestStatus crStatus = changeRequestStatusHelper.saveInitialStatus(
                new ChangeRequestBuilder().withId(1l).build());

        // Check
        assertNotNull(crStatus);
        assertNotNull(crStatus.getId());
        assertEquals(pendingAcbActionStatus, crStatus.getChangeRequestStatusType().getId());
    }

    @Test(expected = EntityRetrievalException.class)
    public void saveInitialStatus_Exception() throws EntityRetrievalException {
        // Setup
        Mockito.when(crStatusDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestStatus.class)))
                .thenThrow(EntityRetrievalException.class);

        // Run
        changeRequestStatusHelper.saveInitialStatus(
                new ChangeRequestBuilder().withId(1l).build());

    }

}
