package old.gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.DeveloperExistenceValidation;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import old.gov.healthit.chpl.changerequest.builders.DeveloperBuilder;

public class DeveloperExistenceValidationTest {

    @Mock
    private DeveloperDAO developerDAO;

    @InjectMocks
    private DeveloperExistenceValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(new DeveloperDTO());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(22l)
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_DevNotInChangeRequest() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(new DeveloperDTO());

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

    @Test
    public void isValid_DevNotInDb() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(22l)
                                .build())
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

}
