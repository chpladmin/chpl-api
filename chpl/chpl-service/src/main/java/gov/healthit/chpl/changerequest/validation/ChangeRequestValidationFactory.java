package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestValidationFactory {

    public final static String CHANGE_REQUEST_TYPE = "CHANGE_REQUEST_TYPE";
    public final static String DEVELOPER = "DEVELOPER";
    public final static String CHANGE_REQUEST_DETAILS_CREATE = "CHANGE_REQUEST_DETAILS_CREATE";
    public final static String CHANGE_REQUEST_DETAILS_UPDATE = "CHANGE_REQUEST_DETAILS_UPDATE";
    public final static String CHANGE_REQUEST_EXISTANCE = "CHANGE_REQUEST_EXISTANCE";
    public final static String STATUS_TYPE = "STATUS_TYPE";

    public ValidationRule<ChangeRequestValidationContext> getRule(String name) {
        switch (name) {
        case CHANGE_REQUEST_TYPE:
            return new ChangeRequestTypeValidation();
        case DEVELOPER:
            return new DeveloperValidation();
        case CHANGE_REQUEST_DETAILS_CREATE:
            return new ChangeRequestDetailsCreateValidation();
        case CHANGE_REQUEST_DETAILS_UPDATE:
            return new ChangeRequestDetailsUpdateValidation();
        case CHANGE_REQUEST_EXISTANCE:
            return new ChangeRequestExistanceValidation();
        case STATUS_TYPE:
            return new CurrentStatusValidation();
        default:
            return null;
        }
    }
}
