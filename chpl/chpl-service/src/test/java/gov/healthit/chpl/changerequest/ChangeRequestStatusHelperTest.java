package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
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
    private ChangeRequestStatusService changeRequestStatusHelper;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledByRequesterStatus;

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

    @Test
    public void updateChangeRequestStatus_Success() throws EntityRetrievalException {
        // Setup
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2019, 9, 1);
        ChangeRequest crFromDB = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withId(3l)
                        .withStatusChangeDate(cal.getTime())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(pendingAcbActionStatus)
                                .withName("Pending ONC-ACB Action")
                                .build())
                        .build())
                .build();

        ChangeRequest crFromCaller = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withStatusChangeDate(cal.getTime())
                        .withComment("This is my comment")
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .withName("Request Approved")
                                .build())
                        .build())
                .build();

        Mockito.when(crStatusTypeDAO.getChangeRequestStatusTypeById(10l))
                .thenReturn(new ChangeRequestStatusTypeBuilder().withId(2l).withName("Request Approved").build());

        Mockito.when(crStatusDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestStatus.class)))
                .thenReturn(new ChangeRequestStatus());

        // Run
        ChangeRequestStatus crStatus = changeRequestStatusHelper.updateChangeRequestStatus(crFromDB,
                crFromCaller);

        // Check
        assertNotNull(crStatus);
    }

    @Test
    public void updateChangeRequestStatus_NoStatusChange() throws EntityRetrievalException {
        // Setup
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2019, 9, 1);
        ChangeRequest crFromDB = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withId(3l)
                        .withStatusChangeDate(cal.getTime())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(pendingAcbActionStatus)
                                .withName("Pending ONC-ACB Action")
                                .build())
                        .build())
                .build();

        ChangeRequest crFromCaller = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withId(3l)
                        .withStatusChangeDate(cal.getTime())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(pendingAcbActionStatus)
                                .withName("Pending ONC-ACB Action")
                                .build())
                        .build())
                .build();

        // Run
        ChangeRequestStatus crStatus = changeRequestStatusHelper.updateChangeRequestStatus(crFromDB,
                crFromCaller);

        // Check
        assertNull(crStatus);
    }

    @Test
    public void updateChangeRequestStatus_StatusChangeNotAllowedDoesNotExist() throws EntityRetrievalException {
        // Setup
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2019, 9, 1);
        ChangeRequest crFromDB = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withId(3l)
                        .withStatusChangeDate(cal.getTime())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(pendingAcbActionStatus)
                                .withName("Pending ONC-ACB Action")
                                .build())
                        .build())
                .build();

        ChangeRequest crFromCaller = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withStatusChangeDate(cal.getTime())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(10l)
                                .withName("Unknown Status")
                                .build())
                        .build())
                .build();

        Mockito.when(crStatusTypeDAO.getChangeRequestStatusTypeById(10l)).thenThrow(EntityRetrievalException.class);
        // Run
        ChangeRequestStatus crStatus = changeRequestStatusHelper.updateChangeRequestStatus(crFromDB,
                crFromCaller);

        // Check
        assertNull(crStatus);
    }

    @Test
    public void updateChangeRequestStatus_StatusChangeNotAllowedCancelled() throws EntityRetrievalException {
        // Setup
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2019, 9, 1);
        ChangeRequest crFromDB = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withId(3l)
                        .withStatusChangeDate(cal.getTime())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(cancelledByRequesterStatus)
                                .withName("Cancelled by Requester")
                                .build())
                        .build())
                .build();

        ChangeRequest crFromCaller = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withStatusChangeDate(cal.getTime())
                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .withName("Request Approved")
                                .build())
                        .build())
                .build();

        Mockito.when(crStatusTypeDAO.getChangeRequestStatusTypeById(10l)).thenThrow(EntityRetrievalException.class);
        // Run
        ChangeRequestStatus crStatus = changeRequestStatusHelper.updateChangeRequestStatus(crFromDB,
                crFromCaller);

        // Check
        assertNull(crStatus);
    }

}
