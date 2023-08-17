package gov.healthit.chpl.subscriber.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class SubscriberValidationService {

    public List<String> validate(SubscriberValidationContext context) {
        return runValidations(gatherValidations(context), context);
    }

    private List<ValidationRule<SubscriberValidationContext>> gatherValidations(SubscriberValidationContext context) {
        List<ValidationRule<SubscriberValidationContext>> rules = new ArrayList<ValidationRule<SubscriberValidationContext>>();
        rules.addAll(getCreateValidations());
        return rules;
    }

    private List<ValidationRule<SubscriberValidationContext>> getCreateValidations() {
        return new ArrayList<ValidationRule<SubscriberValidationContext>>(Arrays.asList(
                new SubscriberUUIDValidation(),
                new SubscriberRoleValidation()));
    }

    private List<String> runValidations(List<ValidationRule<SubscriberValidationContext>> rules, SubscriberValidationContext context) {
        try {
            List<String> errorMessages = new ArrayList<String>();
            for (ValidationRule<SubscriberValidationContext> rule : rules) {
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
