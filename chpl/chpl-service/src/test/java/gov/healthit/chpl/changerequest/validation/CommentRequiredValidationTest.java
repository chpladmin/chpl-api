package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ChangeRequestStatusIds;
import gov.healthit.chpl.domain.Developer;

public class CommentRequiredValidationTest {
    private static final Long REJECTED = 4L;
    private static final Long PENDING_DEVELOPER_ACTION = 2L;
    private static final Long PENDING_ACB_ACTION = 1L;


    private CommentRequiredValidation validator;
    private ChangeRequest newChangeRequest;
    private ChangeRequest origChangeRequest;

    @Before
    public void setup() {
        validator = new CommentRequiredValidation();

        newChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(1L)
                        .build())
                .currentStatus(ChangeRequestStatus.builder()
                        .comment("This is a comment.")
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(2L)
                                .name("Pending Developer Action")
                                .build())
                        .build())
                .build();

        origChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(1L)
                        .build())
                .currentStatus(ChangeRequestStatus.builder()
                        .id(1L)
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(1L)
                                .name("Pending ONC-ACB Action")
                                .build())
                        .build())
                .build();
    }

    @Test
    public void isValid_StatusChangeToPendingDeveloperActionAndCommentIncluded_ReturnTrue() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(null, null, REJECTED, null, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_StatusChangeToRejectedAndCommentIncluded_ReturnTrue() {
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setId(REJECTED);
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setName("Rejected");

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(null, null, REJECTED, null, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_StatusChangeToPendingDeveloperActionAndCommentNotIncluded_ReturnFalse() {
        newChangeRequest.getCurrentStatus().setComment(null);

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(null, null, REJECTED, null, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_StatusChangeToRejectedAndCommentNotIncluded_ReturnFalse() {
        newChangeRequest.getCurrentStatus().setComment(null);
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setId(REJECTED);
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setName("Rejected");

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(null, null, REJECTED, null, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_StatusChangeToPendingAcbActionAndCommentNotIncluded_Returntrue() {
        newChangeRequest.getCurrentStatus().setComment(null);
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setId(PENDING_ACB_ACTION);
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setName("Pending ONC-ACB Action");

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(null, null, REJECTED, null, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_StatusChangeToPendingAcbActionAndCommentIncluded_Returntrue() {
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setId(PENDING_ACB_ACTION);
        newChangeRequest.getCurrentStatus().getChangeRequestStatusType().setName("Pending ONC-ACB Action");

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .origChangeRequest(origChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(null, null, REJECTED, null, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

}
