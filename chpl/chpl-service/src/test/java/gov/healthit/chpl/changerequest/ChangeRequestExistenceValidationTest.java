package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.validation.ChangeRequestExistenceValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestExistenceValidationTest {

    @Mock
    private ChangeRequestDAO crDAO;

    private ChangeRequestExistenceValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        validator = new ChangeRequestExistenceValidation();
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(crDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder().withId(1l).build());

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                new ChangeRequestBuilder().withId(1l).build(),
                crDAO, null, null, null));

        assertTrue(isValid);
    }

    @Test
    public void isValid_Fail_MissingCR() throws EntityRetrievalException {
        Mockito.when(crDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder().withId(1l).build());

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                null, crDAO, null, null, null));

        assertFalse(isValid);
    }

    @Test
    public void isValid_Fail_MissingCRId() throws EntityRetrievalException {
        Mockito.when(crDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder().withId(1l).build());

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                new ChangeRequestBuilder().build(),
                crDAO, null, null, null));

        assertFalse(isValid);
    }

    @Test
    public void isValid_Fail_CRNotInDb() throws EntityRetrievalException {
        Mockito.when(crDAO.get(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        boolean isValid = validator.isValid(new ChangeRequestValidationContext(
                new ChangeRequestBuilder().withId(1l).build(),
                crDAO, null, null, null));

        assertFalse(isValid);
    }

}
