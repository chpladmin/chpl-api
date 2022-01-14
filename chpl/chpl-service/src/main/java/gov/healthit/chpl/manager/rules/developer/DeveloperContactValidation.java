package gov.healthit.chpl.manager.rules.developer;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperContactValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean getErrorMessages(DeveloperValidationContext context) {
        if (context.getDeveloperDTO().getContact() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contactRequired"));
            return false;
        }
        final int startingMessagesSize = getMessages().size();
        if (StringUtils.isEmpty(context.getDeveloperDTO().getContact().getFullName())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contact.nameRequired"));
        }
        if (StringUtils.isEmpty(context.getDeveloperDTO().getContact().getEmail())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contact.emailRequired"));
        }
        if (StringUtils.isEmpty(context.getDeveloperDTO().getContact().getPhoneNumber())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.contact.phoneRequired"));
        }
        return getMessages().size() <= startingMessagesSize;
    }
}
