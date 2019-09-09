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
import gov.healthit.chpl.changerequest.builders.DeveloperBuilder;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.DeveloperValidation;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class DeveloperValidationTest {

    @Mock
    private DeveloperDAO developerDAO;

    private DeveloperValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        validator = new DeveloperValidation();
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(new DeveloperDTO());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(2l)
                                .withCode("0002")
                                .withName("Dev2")
                                .build())
                        .build(),
                null, null, null, developerDAO);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Fail_DevNotPassed() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(new DeveloperDTO());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(null)
                        .build(),
                null, null, null, developerDAO);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

    @Test
    public void isValid_Fail_DevIdNotPassed() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(new DeveloperDTO());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder().build())
                        .build(),
                null, null, null, developerDAO);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

    @Test
    public void isValid_Fail_DevNotInDB() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(2l)
                                .withCode("0002")
                                .withName("Dev2")
                                .build())
                        .build(),
                null, null, null, developerDAO);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

}
