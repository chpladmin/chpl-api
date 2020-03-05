package old.gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.validation.ChangeRequestNotUpdatableDueToStatusValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.exception.EntityRetrievalException;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;

public class ChangeRequestNotUpdatableDueToStatusValidationTest {

    @InjectMocks
    private ChangeRequestNotUpdatableDueToStatusValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(validator, "cancelledStatus", 5l);
        ReflectionTestUtils.setField(validator, "acceptedStatus", 3l);
        ReflectionTestUtils.setField(validator, "rejectedStatus", 4l);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                null,
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(1l)
                                .withStatusChangeDate(new Date())
                                .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .build())
                                .build())
                        .build());

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Fail_CrIsRejected() throws EntityRetrievalException {
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                null,
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(1l)
                                .withStatusChangeDate(new Date())
                                .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(4l) // Rejected
                                        .build())
                                .build())
                        .build());

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }
}
