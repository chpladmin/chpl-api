package gov.healthit.chpl.subscriber.validation;

import java.util.List;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.subscription.domain.SubscriberRole;

public class SubscriberRoleValidation extends ValidationRule<SubscriberValidationContext> {

    @Override
    public boolean isValid(SubscriberValidationContext context) {
        if (context.getRoleId() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscriber.roleRequired"));
            return false;
        }

        List<SubscriberRole> allRoles = context.getSubscriberDao().getAllRoles();
        boolean roleIdExists = allRoles.stream()
            .filter(role -> role.getId().equals(context.getRoleId()))
            .findAny().isPresent();

        if (!roleIdExists) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscriber.roleInvalid",
                    context.getRoleId()));
            return false;
        }

        return true;
    }

}
