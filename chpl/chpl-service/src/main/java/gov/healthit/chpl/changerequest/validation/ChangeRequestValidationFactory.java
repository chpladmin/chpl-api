package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestValidationFactory {

    public final static String CHANGE_REQUEST_TYPE = "CHANGE_REQUEST_TYPE";
    public final static String CHANGE_REQUEST_IN_PROCESS = "CHANGE_REQUEST_IN_PROCESS";
    public final static String DEVELOPER_EXISTENCE = "DEVELOPER_EXISTENCE";
    public final static String DEVELOPER_ACTIVE = "DEVELOPER_ACTIVE";
    public final static String CHANGE_REQUEST_DETAILS_CREATE = "CHANGE_REQUEST_DETAILS_CREATE";
    public final static String CHANGE_REQUEST_DETAILS_UPDATE = "CHANGE_REQUEST_DETAILS_UPDATE";
    public final static String CHANGE_REQUEST_EXISTENCE = "CHANGE_REQUEST_EXISTENCE";
    public final static String STATUS_TYPE = "STATUS_TYPE";
    public final static String STATUS_NOT_UPDATABLE = "STATUS_NOT_UPDATABLE";
    public final static String COMMENT_REQUIRED = "COMMENT_REQUIRED";
    public final static String MULTIPLE_ACBS = "MULTIPLE_ACBS";

    private ChangeRequestDetailsCreateValidation changeRequestDetailsCreateValidation;
    private ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation;
    private ChangeRequestExistenceValidation changeRequestExistanceValidation;
    private ChangeRequestTypeValidation changeRequestTypeValidation;
    private CurrentStatusValidation currentStatusValidation;
    private DeveloperExistenceValidation developerExistenceValidation;
    private DeveloperActiveValidation developerActiveValidation;
    private ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation;
    private ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation;
    private CommentRequiredValidation commentRequiredValidation;
    private RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation;

    @Autowired
    public ChangeRequestValidationFactory(
            final ChangeRequestDetailsCreateValidation changeRequestDetailsCreateValidation,
            final ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation,
            final ChangeRequestExistenceValidation changeRequestExistanceValidation,
            final ChangeRequestTypeValidation changeRequestTypeValidation,
            final CurrentStatusValidation currentStatusValidation,
            final DeveloperExistenceValidation developerExistenceValidation,
            final DeveloperActiveValidation developerActiveValidation,
            final ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation,
            final ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation,
            final CommentRequiredValidation commentRequiredValidation,
            final RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation) {
        this.changeRequestDetailsCreateValidation = changeRequestDetailsCreateValidation;
        this.changeRequestDetailsUpdateValidation = changeRequestDetailsUpdateValidation;
        this.changeRequestExistanceValidation = changeRequestExistanceValidation;
        this.changeRequestTypeValidation = changeRequestTypeValidation;
        this.currentStatusValidation = currentStatusValidation;
        this.developerExistenceValidation = developerExistenceValidation;
        this.developerActiveValidation = developerActiveValidation;
        this.changeRequestNotUpdatableDueToStatusValidation = changeRequestNotUpdatableDueToStatusValidation;
        this.changeRequestTypeInProcessValidation = changeRequestTypeInProcessValidation;
        this.commentRequiredValidation = commentRequiredValidation;
        this.roleAcbHasMultipleCertificationBodiesValidation = roleAcbHasMultipleCertificationBodiesValidation;
    }

    public ValidationRule<ChangeRequestValidationContext> getRule(String name) {
        switch (name) {
        case CHANGE_REQUEST_TYPE:
            return changeRequestTypeValidation;
        case CHANGE_REQUEST_IN_PROCESS:
            return changeRequestTypeInProcessValidation;
        case DEVELOPER_EXISTENCE:
            return developerExistenceValidation;
        case DEVELOPER_ACTIVE:
            return developerActiveValidation;
        case CHANGE_REQUEST_DETAILS_CREATE:
            return changeRequestDetailsCreateValidation;
        case CHANGE_REQUEST_DETAILS_UPDATE:
            return changeRequestDetailsUpdateValidation;
        case CHANGE_REQUEST_EXISTENCE:
            return changeRequestExistanceValidation;
        case STATUS_TYPE:
            return currentStatusValidation;
        case STATUS_NOT_UPDATABLE:
            return changeRequestNotUpdatableDueToStatusValidation;
        case COMMENT_REQUIRED:
            return commentRequiredValidation;
        case MULTIPLE_ACBS:
            return roleAcbHasMultipleCertificationBodiesValidation;
        default:
            return null;
        }
    }
}
