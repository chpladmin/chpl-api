package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestValidator {
    private ChangeRequestValidationFactory crValidationFactory;

    @Autowired
    public ChangeRequestValidator(ChangeRequestValidationFactory crValidationFactory) {
        this.crValidationFactory = crValidationFactory;
    }

    public List<String> validate(ChangeRequestValidationContext context) {
        List<String> errors = new ArrayList<String>();

        if (isNewChangeRequest(context)) {
            errors.addAll(runCreateValidations(context));
        } else {
            errors.addAll(runUpdateValidations(context));
        }

        return errors;
    }


    private Boolean isNewChangeRequest(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest() == null;
    }

    private List<String> runCreateValidations(ChangeRequestValidationContext context) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_TYPE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_IN_PROCESS));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_EXISTENCE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_ACTIVE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_CREATE));
        return runValidations(rules, context);
    }

    private List<String> runUpdateValidations(ChangeRequestValidationContext context) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_UPDATE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.MULTIPLE_ACBS));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_ACTIVE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.STATUS_TYPE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.STATUS_NOT_UPDATABLE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.COMMENT_REQUIRED));
        return runValidations(rules, context);
    }

    private List<String> runValidations(List<ValidationRule<ChangeRequestValidationContext>> rules, ChangeRequestValidationContext context) {
        try {
            List<String> errorMessages = new ArrayList<String>();
            for (ValidationRule<ChangeRequestValidationContext> rule : rules) {
                if (rule != null && !rule.isValid(context)) {
                    errorMessages.addAll(rule.getMessages());
                }
            }
            return errorMessages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
