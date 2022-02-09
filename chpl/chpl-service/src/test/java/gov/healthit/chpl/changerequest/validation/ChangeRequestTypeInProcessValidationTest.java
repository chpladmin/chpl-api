package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ChangeRequestStatusIds;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ValidationDAOs;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestTypeInProcessValidationTest {
    private static final Long PENDING_ACB_ACTION = 1L;
    private static final Long PENDING_DEVELOPER_ACTION = 2L;
    private static final Long ACCEPTED = 3L;
    private static final Long REJECTED = 4L;
    private static final Long CANCELLED_BY_REQUESTER = 5L;

    private static final Long ATTESTATION_CHANGE_REQUEST_TYPE = 3L;
    private static final Long WEBSITE_CHANGE_REQUEST_TYPE = 1L;

    private ChangeRequestTypeInProcessValidation validator;
    private ChangeRequest newChangeRequest;

    @Before
    public void before() {
        validator = new ChangeRequestTypeInProcessValidation();

        newChangeRequest = ChangeRequest.builder()
                .changeRequestType(ChangeRequestType.builder()
                        .id(ATTESTATION_CHANGE_REQUEST_TYPE)
                        .build())
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .build();
    }


    @Test
    public void isValid_DeveloperHasNoOtherChangeRequests_ReturnsTrue() throws EntityRetrievalException {
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong())).thenReturn(new ArrayList<ChangeRequest>());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .validationDAOs(new ValidationDAOs(null, changeRequestDAO, null, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_DeveloperHasNoOtherChangeRequestsInProcess_ReturnsTrue() throws EntityRetrievalException {
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(ChangeRequest.builder()
                        .changeRequestType(ChangeRequestType.builder()
                                .id(ATTESTATION_CHANGE_REQUEST_TYPE)
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(ACCEPTED)
                                        .build())
                                .build())
                        .build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .validationDAOs(new ValidationDAOs(null, changeRequestDAO, null, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_DeveloperHasOtherSameRequestInProcess_ReturnsFalse() throws EntityRetrievalException {
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(ChangeRequest.builder()
                        .changeRequestType(ChangeRequestType.builder()
                                .id(ATTESTATION_CHANGE_REQUEST_TYPE)
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(PENDING_ACB_ACTION)
                                        .build())
                                .build())
                        .build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .validationDAOs(new ValidationDAOs(null, changeRequestDAO, null, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);

    }

    @Test
    public void isValid_DeveloperHasOtherChangeRequestTypeInProcess_ReturnsTrue() throws EntityRetrievalException {
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(ChangeRequest.builder()
                        .changeRequestType(ChangeRequestType.builder()
                                .id(WEBSITE_CHANGE_REQUEST_TYPE)
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(PENDING_ACB_ACTION)
                                        .build())
                                .build())
                        .build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .validationDAOs(new ValidationDAOs(null, changeRequestDAO, null, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);

    }
}
