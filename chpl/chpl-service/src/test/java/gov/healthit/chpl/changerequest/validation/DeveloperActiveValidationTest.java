package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ValidationDAOs;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class DeveloperActiveValidationTest {

    private ChangeRequest newChangeRequest;
    private ChangeRequest origChangeRequest;
    private ChangeRequestDAO changeRequestDAO;
    private DeveloperDAO developerDAO;

    @Before
    public void setup() throws EntityRetrievalException {
        changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(
                ChangeRequest.builder()
                    .developer(Developer.builder()
                            .developerId(1L)
                            .build())
                    .build());

        developerDAO = Mockito.mock(DeveloperDAO.class);
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(
                DeveloperDTO.builder()
                        .id(1L)
                        .statusEvents(Arrays.asList(DeveloperStatusEventDTO.builder()
                                .id(1L)
                                .developerId(1L)
                                .status(DeveloperStatusDTO.builder()
                                        .id(1L)
                                        .statusName("Active")
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

        origChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .build();
    }

    @Test
    public void isValid_DeveloperStatusValidForNewChangeRequest_ReturnsTrue() throws EntityRetrievalException {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .validationDAOs(new ValidationDAOs(developerDAO, changeRequestDAO, null, null))
                .build();

        DeveloperActiveValidation validation = new DeveloperActiveValidation();

        Boolean isValid = validation.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_DeveloperStatusValidForExistingChangeRequest_ReturnsTrue() throws EntityRetrievalException {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .validationDAOs(new ValidationDAOs(developerDAO, changeRequestDAO, null, null))
                .build();

        DeveloperActiveValidation validation = new DeveloperActiveValidation();

        Boolean isValid = validation.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_DeveloperStatusInvalidChangeRequest_ReturnsFalse() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(
                DeveloperDTO.builder()
                        .id(1L)
                        .statusEvents(Arrays.asList(DeveloperStatusEventDTO.builder()
                                .id(1L)
                                .developerId(1L)
                                .status(DeveloperStatusDTO.builder()
                                        .id(2L)
                                        .statusName("Suspended by ONC")
                                        .build())
                                .statusDate(new Date())
                                .build()))
                        .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .validationDAOs(new ValidationDAOs(developerDAO, changeRequestDAO, null, null))
                .build();

        DeveloperActiveValidation validation = new DeveloperActiveValidation();

        Boolean isValid = validation.isValid(context);

        assertEquals(false, isValid);
    }

}
