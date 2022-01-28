package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ChangeRequestStatusIds;

public class ChangeRequestNotUpdatableDueToStatusValidationTest {
    private static final Long PENDING_ACB_ACTION = 1L;
    private static final Long PENDING_DEVELOPER_ACTION = 2L;
    private static final Long ACCEPTED = 3L;
    private static final Long REJECTED = 4L;
    private static final Long CANCELLED_BY_REQUESTER = 5L;

    private ChangeRequestNotUpdatableDueToStatusValidation validator;

    @Before
    public void before() {
        validator = new ChangeRequestNotUpdatableDueToStatusValidation();
    }

    @Test
    public void isValid_CurrentStatusIsPendingAcbAction_ReturnsTrue() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(ChangeRequest.builder()
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(PENDING_ACB_ACTION)
                                        .build())
                                .build())
                        .build())
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_CurrentStatusIsPendingDeveloperAction_ReturnsTrue() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(ChangeRequest.builder()
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(PENDING_DEVELOPER_ACTION)
                                        .build())
                                .build())
                        .build())
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_CurrentStatusIsAccepted_ReturnsFalse() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(ChangeRequest.builder()
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(ACCEPTED)
                                        .build())
                                .build())
                        .build())
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_CurrentStatusIsRejected_ReturnsFalse() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(ChangeRequest.builder()
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(REJECTED)
                                        .build())
                                .build())
                        .build())
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_CurrentStatusIsCancelledByRequester_ReturnsFalse() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(ChangeRequest.builder()
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(CANCELLED_BY_REQUESTER)
                                        .build())
                                .build())
                        .build())
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }
}
