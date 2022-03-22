package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ValidationDAOs;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class DeveloperExistenceValidationTest {
    private DeveloperExistenceValidation validator;
    private ChangeRequest newChangeRequest;
    private DeveloperDAO developerDAO;

    @Before
    public void setup() throws EntityRetrievalException {
        validator = new DeveloperExistenceValidation();

        developerDAO = Mockito.mock(DeveloperDAO.class);
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(
                Developer.builder()
                        .id(1L)
                        .statusEvents(Arrays.asList(DeveloperStatusEvent.builder()
                                .id(1L)
                                .developerId(1L)
                                .status(DeveloperStatus.builder()
                                        .id(1L)
                                        .status("Active")
                                        .build())
                                .statusDate(new Date())
                                .build()))
                        .build());

        newChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .build();

    }

    @Test
    public void isValid_DeveloperExists_ReturnsTrue() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .validationDAOs(new ValidationDAOs(developerDAO, null, null, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_DeveloperDoesNot_ReturnsFalse() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenThrow(new EntityRetrievalException());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .validationDAOs(new ValidationDAOs(developerDAO, null, null, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_DeveloperNotPasssed_ReturnsFalse() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenThrow(new EntityRetrievalException());

        newChangeRequest.setDeveloper(null);

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .validationDAOs(new ValidationDAOs(developerDAO, null, null, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }
}
