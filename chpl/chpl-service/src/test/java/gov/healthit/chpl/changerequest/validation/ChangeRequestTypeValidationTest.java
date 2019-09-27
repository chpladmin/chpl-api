package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestTypeBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestTypeValidationTest {

    @Mock
    private ChangeRequestTypeDAO changeRequestTypeDAO;

    @InjectMocks
    private ChangeRequestTypeValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(changeRequestTypeDAO.getChangeRequestTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestTypeBuilder().withId(1l).withName("First Status").build());

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withChangeRequestType(new ChangeRequestTypeBuilder()
                                .withId(1l).withName("First Status").build())
                        .build(),
                null));

        assertTrue(isValid);
    }

    @Test
    public void isValid_Fail_ChangeRequestTypeNull() throws EntityRetrievalException {
        Mockito.when(changeRequestTypeDAO.getChangeRequestTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestTypeBuilder().withId(1l).withName("First Status").build());

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .build(),
                null));

        assertFalse(isValid);
    }

    @Test
    public void isValid_Fail_ChangeRequestTypeIdNull() throws EntityRetrievalException {
        Mockito.when(changeRequestTypeDAO.getChangeRequestTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestTypeBuilder().withId(1l).withName("First Status").build());

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withChangeRequestType(new ChangeRequestTypeBuilder()
                                .withName("First Status").build())
                        .build(),
                null));

        assertFalse(isValid);
    }

    @Test
    public void isValid_Fail_ChangeRequetTypeNotInDb() throws EntityRetrievalException {
        Mockito.when(changeRequestTypeDAO.getChangeRequestTypeById(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withChangeRequestType(new ChangeRequestTypeBuilder()
                                .withId(1l).withName("First Status").build())
                        .build(),
                null));

        assertFalse(isValid);
    }

}
