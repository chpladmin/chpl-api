package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ChangeRequestStatusIds;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ValidationDAOs;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class CurrentStatusValidationTest {
    private static final Long PENDING_ACB_ACTION = 1L;
    private static final Long PENDING_DEVELOPER_ACTION = 2L;
    private static final Long ACCEPTED = 3L;
    private static final Long REJECTED = 4L;
    private static final Long CANCELLED_BY_REQUESTER = 5L;

    private CurrentStatusValidation validator;
    private ChangeRequest newChangeRequest;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private ResourcePermissions resourcePermissions;

    @Before
    public void before() throws EntityRetrievalException {
        validator = new CurrentStatusValidation();
        resourcePermissions = Mockito.mock(ResourcePermissions.class);

        changeRequestStatusTypeDAO = Mockito.mock(ChangeRequestStatusTypeDAO.class);
        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequestStatusType.builder().build());

        newChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .build();

    }

    @Test
    public void isValid_NewCRDoesNotHaveCurrentStatus_ReturnTrue() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_NewCRDoesNotHaveValidCurrentStatus_ReturnFalse() throws EntityRetrievalException {
        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenThrow(new EntityRetrievalException());

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(99L)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_SubmittedByDeveloperWithStatusOfCancelled_ReturnTrue() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(CANCELLED_BY_REQUESTER)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_SubmittedByDeveloperWithStatusOfPendingAcbAction_ReturnTrue() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(PENDING_ACB_ACTION)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);

    }

    @Test
    public void isValid_SubmittedByAcbithStatusOfCancelled_ReturnFalse() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(CANCELLED_BY_REQUESTER)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_SubmittedByAcbWithStatusOfPendingAcvAction_ReturnFalse() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(CANCELLED_BY_REQUESTER)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);

    }

    @Test
    public void isValid_SubmittedByAcbWithStatusOfAccepted_ReturnTrue() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(ACCEPTED)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_SubmittedByAcbWithStatusOfRejected_ReturnTrue() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(REJECTED)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_SubmittedByAcbWithStatusOfPenddingDeveloperAction_ReturnTrue() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(PENDING_DEVELOPER_ACTION)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }
    @Test
    public void isValid_SubmittedByDeveloperbWithStatusOfAccepted_ReturnFalse() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(ACCEPTED)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_SubmittedByDeveloperWithStatusOfRejected_ReturnFalse() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(REJECTED)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_SubmittedByDeveloperWithStatusOfPenddingDeveloperAction_ReturnFalse() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        newChangeRequest.setCurrentStatus(ChangeRequestStatus.builder()
                .changeRequestStatusType(ChangeRequestStatusType.builder()
                        .id(PENDING_DEVELOPER_ACTION)
                        .build())
                .build());

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .resourcePermissions(resourcePermissions)
                .validationDAOs(new ValidationDAOs(null, null, changeRequestStatusTypeDAO, null))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }
}
