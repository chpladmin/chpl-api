package gov.healthit.chpl.subscription.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class SubscriptionRequestValidationService {

    public List<String> validate(SubscriptionRequestValidationContext context) {
        return runValidations(gatherValidations(context), context);
    }

    private List<ValidationRule<SubscriptionRequestValidationContext>> gatherValidations(SubscriptionRequestValidationContext context) {
        List<ValidationRule<SubscriptionRequestValidationContext>> rules = new ArrayList<ValidationRule<SubscriptionRequestValidationContext>>();
        rules.addAll(getCreateValidations());
        return rules;
    }

    private List<ValidationRule<SubscriptionRequestValidationContext>> getCreateValidations() {
        return new ArrayList<ValidationRule<SubscriptionRequestValidationContext>>(Arrays.asList(
                new EmailAddressValidation(),
                new SubscriptionReasonValidation(),
                new SubscriptionObjectTypeValidation(),
                new SubscribedObjectValidation()));
    }

    private List<String> runValidations(List<ValidationRule<SubscriptionRequestValidationContext>> rules, SubscriptionRequestValidationContext context) {
        try {
            List<String> errorMessages = new ArrayList<String>();
            for (ValidationRule<SubscriptionRequestValidationContext> rule : rules) {
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
