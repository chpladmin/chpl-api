package gov.healthit.chpl.subscription.validation;

import java.util.List;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;

public class SubscriptionObjectTypeValidation extends ValidationRule<SubscriptionRequestValidationContext> {

    @Override
    public boolean isValid(SubscriptionRequestValidationContext context) {
        if (context.getSubscriptionRequest().getSubscribedObjectTypeId() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectTypeIdRequired"));
            return false;
        }

        List<SubscriptionObjectType> allSubscriptionObjectTypes = context.getSubscriptionDao().getAllSubscriptionObjectTypes();
        boolean objectTypeIdExists = allSubscriptionObjectTypes.stream()
            .filter(objType -> objType.getId().equals(context.getSubscriptionRequest().getSubscribedObjectTypeId()))
            .findAny().isPresent();

        if (!objectTypeIdExists) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectTypeIdInvalid",
                    context.getSubscriptionRequest().getSubscribedObjectTypeId()));
            return false;
        }
        return true;
    }

}
