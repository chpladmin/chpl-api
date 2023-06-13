package gov.healthit.chpl.subscriber.validation;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.subscription.domain.Subscriber;

public class SubscriberUUIDValidation extends ValidationRule<SubscriberValidationContext> {

    @Override
    public boolean isValid(SubscriberValidationContext context) {
        if (context.getSubscriberId() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscriber.idRequired"));
            return false;
        }

        if (getSubscriberById(context) == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscriber.idInvalid",
                    context.getSubscriberId().toString()));
            return false;
        }

        return true;
    }

    private Subscriber getSubscriberById(SubscriberValidationContext context) {
        return context.getSubscriberDao().getSubscriberById(context.getSubscriberId());
    }
}
