package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestValidationFactory {

    public final static String CHANGE_REQUEST_TYPE = "CHANGE_REQUEST_TYPE";
    public final static String DEVELOPER = "DEVELOPER";
    public final static String CHANGE_REQUEST_DETAILS_CREATE = "CHANGE_REQUEST_DETAILS_CREATE";
    public final static String CHANGE_REQUEST_DETAILS_UPDATE = "CHANGE_REQUEST_DETAILS_UPDATE";
    public final static String CHANGE_REQUEST_EXISTENCE = "CHANGE_REQUEST_EXISTENCE";
    public final static String STATUS_TYPE = "STATUS_TYPE";
    public final static String STATUS_APPROVED = "STATUS_APPROVED";
    public final static String STATUS_REJECTED = "STATUS_REJECTED";

    private ChangeRequestDetailsCreateValidation changeRequestDetailsCreateValidation;
    private ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation;
    private ChangeRequestExistenceValidation changeRequestExistanceValidation;
    private ChangeRequestTypeValidation changeRequestTypeValidation;
    private CurrentStatusValidation currentStatusValidation;
    private DeveloperValidation developerValidation;
    private ChangeRequestApprovedValidation changeRequestApprovedValidation;
    private ChangeRequestRejectedValidation changeRequestRejectedValidation;

    @Autowired
    public ChangeRequestValidationFactory(
            final ChangeRequestDetailsCreateValidation changeRequestDetailsCreateValidation,
            final ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation,
            final ChangeRequestExistenceValidation changeRequestExistanceValidation,
            final ChangeRequestTypeValidation changeRequestTypeValidation,
            final CurrentStatusValidation currentStatusValidation,
            final DeveloperValidation developerValidation,
            final ChangeRequestApprovedValidation changeRequestApprovedValidation,
            final ChangeRequestRejectedValidation changeRequestRejectedValidation) {
        this.changeRequestDetailsCreateValidation = changeRequestDetailsCreateValidation;
        this.changeRequestDetailsUpdateValidation = changeRequestDetailsUpdateValidation;
        this.changeRequestExistanceValidation = changeRequestExistanceValidation;
        this.changeRequestTypeValidation = changeRequestTypeValidation;
        this.currentStatusValidation = currentStatusValidation;
        this.developerValidation = developerValidation;
        this.changeRequestApprovedValidation = changeRequestApprovedValidation;
        this.changeRequestRejectedValidation = changeRequestRejectedValidation;
    }

    public ValidationRule<ChangeRequestValidationContext> getRule(String name) {
        switch (name) {
        case CHANGE_REQUEST_TYPE:
            return changeRequestTypeValidation;
        case DEVELOPER:
            return developerValidation;
        case CHANGE_REQUEST_DETAILS_CREATE:
            return changeRequestDetailsCreateValidation;
        case CHANGE_REQUEST_DETAILS_UPDATE:
            return changeRequestDetailsUpdateValidation;
        case CHANGE_REQUEST_EXISTENCE:
            return changeRequestExistanceValidation;
        case STATUS_TYPE:
            return currentStatusValidation;
        case STATUS_APPROVED:
            return changeRequestApprovedValidation;
        case STATUS_REJECTED:
            return changeRequestRejectedValidation;
        default:
            return null;
        }
    }
}
