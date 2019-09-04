package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestValidationFactory {
    public final static String CHANGE_REQUEST = "CHANGE_REQUEST";

    public ValidationRule<ChangeRequestValidationContext> getRule(String name) {
        switch (name) {
        case CHANGE_REQUEST:
            return new ChangeRequestTypeValidation();
        default:
            return null;
        }
    }
}
