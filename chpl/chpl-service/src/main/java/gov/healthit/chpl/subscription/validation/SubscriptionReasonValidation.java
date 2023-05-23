package gov.healthit.chpl.subscription.validation;

import java.util.List;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.subscription.domain.SubscriptionReason;

public class SubscriptionReasonValidation extends ValidationRule<SubscriptionRequestValidationContext> {

    @Override
    public boolean isValid(SubscriptionRequestValidationContext context) {
        if (context.getSubscriptionRequest().getReasonId() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.reasonRequired"));
            return false;
        }

        List<SubscriptionReason> allReasons = context.getSubscriptionDao().getAllReasons();
        boolean reasonIdExists = allReasons.stream()
            .filter(reason -> reason.getId().equals(context.getSubscriptionRequest().getReasonId()))
            .findAny().isPresent();

        if (!reasonIdExists) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.reasonInvalid",
                    context.getSubscriptionRequest().getReasonId()));
            return false;
        }

        return true;
    }

}
