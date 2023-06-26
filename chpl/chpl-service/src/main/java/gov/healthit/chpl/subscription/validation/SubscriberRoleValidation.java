package gov.healthit.chpl.subscription.validation;

import java.util.List;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.subscription.domain.SubscriberRole;

public class SubscriberRoleValidation extends ValidationRule<SubscriptionRequestValidationContext> {

    @Override
    public boolean isValid(SubscriptionRequestValidationContext context) {
        if (context.getSubscriptionRequest().getRoleId() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.roleRequired"));
            return false;
        }

        List<SubscriberRole> allRoles = context.getSubscriberDao().getAllRoles();
        boolean roleIdExists = allRoles.stream()
            .filter(role -> role.getId().equals(context.getSubscriptionRequest().getRoleId()))
            .findAny().isPresent();

        if (!roleIdExists) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.roleInvalid",
                    context.getSubscriptionRequest().getRoleId()));
            return false;
        }

        return true;
    }

}
