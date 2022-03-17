package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperContactValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (context.getDeveloper().getContact() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contactRequired"));
            return false;
        }
        final int startingMessagesSize = getMessages().size();
        if (ObjectUtils.isEmpty(context.getDeveloper().getContact().getFullName())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contact.nameRequired"));
        }
        if (ObjectUtils.isEmpty(context.getDeveloper().getContact().getEmail())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contact.emailRequired"));
        }
        if (ObjectUtils.isEmpty(context.getDeveloper().getContact().getPhoneNumber())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contact.phoneRequired"));
        }
        return getMessages().size() <= startingMessagesSize;
    }
}
