package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ValidationDAOs;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestTypeValidationTest {
    private ChangeRequestTypeValidation validator;
    private ChangeRequest newChangeRequest;

    @Before
    public void setup() {
        validator = new ChangeRequestTypeValidation();
    }

    @Test
    public void isValid_CrTypeIsValid_ReturnTrue() throws EntityRetrievalException {
        newChangeRequest = ChangeRequest.builder()
                .changeRequestType(ChangeRequestType.builder()
                        .id(3L)
                        .name("Attestation")
                        .build())
                .build();

        ChangeRequestTypeDAO changeRequestTypeDAO = Mockito.mock(ChangeRequestTypeDAO.class);
        Mockito.when(changeRequestTypeDAO.getChangeRequestTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequestType.builder()
                        .id(3L)
                        .name("Attestation")
                        .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .validationDAOs(new ValidationDAOs(null, null, null, changeRequestTypeDAO))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);

    }

    @Test
    public void isValid_CrTypeDoesNotExistInDb_ReturnFalse() throws EntityRetrievalException {
        newChangeRequest = ChangeRequest.builder()
                .changeRequestType(ChangeRequestType.builder()
                        .id(99L)
                        .name("Invalid Type")
                        .build())
                .build();

        ChangeRequestTypeDAO changeRequestTypeDAO = Mockito.mock(ChangeRequestTypeDAO.class);
        Mockito.when(changeRequestTypeDAO.getChangeRequestTypeById(ArgumentMatchers.anyLong()))
                .thenThrow(new EntityRetrievalException());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .validationDAOs(new ValidationDAOs(null, null, null, changeRequestTypeDAO))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_CrTypeNotInCr_ReturnFalse() {
        newChangeRequest = ChangeRequest.builder()
                .build();

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }
}
