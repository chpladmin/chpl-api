package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.validation.ChangeRequestNotUpdatableDueToStatusValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestNotUpdatableDueToStatusValidationTest {

    @Mock
    private ChangeRequestDAO changeRequestDAO;

    private ChangeRequestNotUpdatableDueToStatusValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        validator = new ChangeRequestNotUpdatableDueToStatusValidation();
        ReflectionTestUtils.setField(validator, "cancelledStatus", 5l);
        ReflectionTestUtils.setField(validator, "acceptedStatus", 3l);
        ReflectionTestUtils.setField(validator, "rejectedStatus", 4l);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(1l)
                                .withStatusChangeDate(new Date())
                                .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .build())
                                .build())
                        .build());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder().withId(1l).build(), changeRequestDAO, null, null, null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Fail_CrIsRejected() throws EntityRetrievalException {
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(1l)
                                .withStatusChangeDate(new Date())
                                .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(4l) // Rejected
                                        .build())
                                .build())
                        .build());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder().withId(1l).build(), changeRequestDAO, null, null, null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }
}
