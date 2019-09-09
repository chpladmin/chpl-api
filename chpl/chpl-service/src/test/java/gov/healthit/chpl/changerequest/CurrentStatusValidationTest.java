package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.CurrentStatusValidation;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class CurrentStatusValidationTest {

    @Mock
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;

    private CurrentStatusValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        validator = new CurrentStatusValidation();
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestStatusTypeBuilder().withId(1l).withName("Name").build());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(4l)
                                .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .withName("Status 1")
                                        .build())
                                .build())
                        .build(),
                null, null, changeRequestStatusTypeDAO, null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Success_CurrentStatusNull() throws EntityRetrievalException {
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .build(),
                null, null, changeRequestStatusTypeDAO, null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Success_StatusTypeNull() throws EntityRetrievalException {
        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestStatusTypeBuilder().withId(1l).withName("Name").build());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(4l)
                                .build())
                        .build(),
                null, null, changeRequestStatusTypeDAO, null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Failure_StatusTypeNotValid() throws EntityRetrievalException {
        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(4l)
                                .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .withName("Status 1")
                                        .build())
                                .build())
                        .build(),
                null, null, changeRequestStatusTypeDAO, null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
        assertEquals(1, validator.getMessages().size());
    }

}
