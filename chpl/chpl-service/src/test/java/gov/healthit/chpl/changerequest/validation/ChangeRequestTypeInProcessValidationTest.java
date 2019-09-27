package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

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
import gov.healthit.chpl.changerequest.builders.DeveloperBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestTypeInProcessValidationTest {
    @Mock
    private ChangeRequestDAO changeRequestDAO;

    @InjectMocks
    private ChangeRequestTypeInProcessValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(validator, "pendingAcbAction", 1l);
        ReflectionTestUtils.setField(validator, "pendingDeveloperAction", 2l);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong()))
                .thenReturn(new ArrayList<ChangeRequest>(Arrays.asList(
                        new ChangeRequestBuilder()
                                .withId(1l)
                                .withCurrentStatus(new ChangeRequestStatusBuilder()
                                        .withId(23l)
                                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                                .withId(5l)
                                                .build())
                                        .build())
                                .build())));

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(44l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(345l)
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Success_MultipleCrs() throws EntityRetrievalException {
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong()))
                .thenReturn(new ArrayList<ChangeRequest>(Arrays.asList(
                        new ChangeRequestBuilder()
                                .withId(1l)
                                .withCurrentStatus(new ChangeRequestStatusBuilder()
                                        .withId(23l)
                                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                                .withId(5l)
                                                .build())
                                        .build())
                                .build(),
                        new ChangeRequestBuilder()
                                .withId(2l)
                                .withCurrentStatus(new ChangeRequestStatusBuilder()
                                        .withId(23l)
                                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                                .withId(4l)
                                                .build())
                                        .build())
                                .build())));

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(44l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(345l)
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Fail_InProcess() throws EntityRetrievalException {
        // Return a CR that is in process
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong()))
                .thenReturn(new ArrayList<ChangeRequest>(Arrays.asList(
                        new ChangeRequestBuilder()
                                .withId(1l)
                                .withCurrentStatus(new ChangeRequestStatusBuilder()
                                        .withId(23l)
                                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                                .withId(1l)
                                                .build())
                                        .build())
                                .build())));

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(44l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(345l)
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

    @Test
    public void isValid_Fail_MultipleInProcess() throws EntityRetrievalException {
        // Return a CR that is in process
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong()))
                .thenReturn(new ArrayList<ChangeRequest>(Arrays.asList(
                        new ChangeRequestBuilder()
                                .withId(1l)
                                .withCurrentStatus(new ChangeRequestStatusBuilder()
                                        .withId(23l)
                                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                                .withId(1l)
                                                .build())
                                        .build())
                                .build(),
                        new ChangeRequestBuilder()
                                .withId(1l)
                                .withCurrentStatus(new ChangeRequestStatusBuilder()
                                        .withId(23l)
                                        .withChangeReequestStatusType(new ChangeRequestStatusTypeBuilder()
                                                .withId(5l)
                                                .build())
                                        .build())
                                .build())));

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(44l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(345l)
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

}
