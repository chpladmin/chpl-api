package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ValidationDAOs;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class DeveloperStatusValidationTest {

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
                            .id(1L)
                            .build())
                    .build());

        developerDAO = Mockito.mock(DeveloperDAO.class);
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(
                Developer.builder()
                        .id(1L)
                        .build());

        newChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(1L)
                        .build())
                .build();

        origChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(1L)
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
                Developer.builder()
                        .id(1L)
                        .statuses(Arrays.asList(DeveloperStatusEvent.builder()
                                .id(1L)
                                .status(DeveloperStatus.builder()
                                        .id(2L)
                                        .name("Suspended by ONC")
                                        .build())
                                .startDay(LocalDate.now())
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
