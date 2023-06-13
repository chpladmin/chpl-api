package gov.healthit.chpl.subscription.validation;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.validator.routines.EmailValidator;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class EmailAddressValidation extends ValidationRule<SubscriptionRequestValidationContext> {

    @Override
    public boolean isValid(SubscriptionRequestValidationContext context) {
        if (ObjectUtils.isEmpty(context.getSubscriptionRequest().getEmail())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.emailRequired"));
            return false;
        }

        boolean isEmailValid = EmailValidator.getInstance().isValid(context.getSubscriptionRequest().getEmail());
        if (!isEmailValid) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.emailInvalid",
                    context.getSubscriptionRequest().getEmail()));
            return false;
        }
        return true;
    }

}
