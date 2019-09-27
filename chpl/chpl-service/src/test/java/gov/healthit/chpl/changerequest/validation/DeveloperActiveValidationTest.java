package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.DeveloperBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class DeveloperActiveValidationTest {
    @Mock
    private DeveloperDAO developerDAO;

    @Mock
    private ChangeRequestDAO changeRequestDAO;

    @InjectMocks
    private DeveloperActiveValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getActiveDeveloper());
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(22l)
                                .build())
                        .build());

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
    public void isValid_NotActive() throws EntityRetrievalException {
        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getInactiveDeveloper());

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder()
                                .withId(22l)
                                .build())
                        .build());

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

    private DeveloperDTO getActiveDeveloper() {
        DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
        statusEvent.setId(1l);
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setId(1l);
        status.setStatusName("Active");
        statusEvent.setStatus(status);
        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(1l);
        dto.setStatusEvents(new ArrayList<DeveloperStatusEventDTO>());
        dto.getStatusEvents().add(statusEvent);
        return dto;
    }

    private DeveloperDTO getInactiveDeveloper() {
        DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
        statusEvent.setId(1l);
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setId(2l);
        status.setStatusName("Not Active");
        statusEvent.setStatus(status);
        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(1l);
        dto.setStatusEvents(new ArrayList<DeveloperStatusEventDTO>());
        dto.getStatusEvents().add(statusEvent);
        return dto;
    }

}
