package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class CurrentStatusValidationTest {

    @Mock
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CurrentStatusValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(validator, "pendingAcbActionStatus", 1l);
        ReflectionTestUtils.setField(validator, "pendingDeveloperActionStatus", 2l);
        ReflectionTestUtils.setField(validator, "cancelledStatus", 5l);
        ReflectionTestUtils.setField(validator, "acceptedStatus", 3l);
        ReflectionTestUtils.setField(validator, "rejectedStatus", 4l);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestStatusTypeBuilder().withId(1l).withName("Name").build());

        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin())
                .thenReturn(true);

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(4l)
                                .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .withName("Status 1")
                                        .build())
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Success_CurrentStatusNull() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin())
                .thenReturn(true);

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Success_StatusTypeNull() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin())
                .thenReturn(true);

        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestStatusTypeBuilder().withId(1l).withName("Name").build());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(4l)
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Failure_StatusTypeNotValid() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin())
                .thenReturn(true);

        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(4l)
                                .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(1l)
                                        .withName("Status 1")
                                        .build())
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_Failure_StatusTypeNotValidForRoleDeveloper() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin())
                .thenReturn(true);

        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestStatusTypeBuilder().withId(2l).withName("Name").build());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(1l)
                                .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(2l)
                                        .build())
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_Failure_StatusTypeNotValidForRoleAdmin() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin())
                .thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin())
                .thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc())
                .thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin())
                .thenReturn(false);

        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestStatusTypeBuilder().withId(5l).withName("Name").build());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withCurrentStatus(new ChangeRequestStatusBuilder()
                                .withId(1l)
                                .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                        .withId(5l)
                                        .build())
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
        assertEquals(1, validator.getMessages().size());
    }

}
