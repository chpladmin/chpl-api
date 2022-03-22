package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperAddressValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (context.getDeveloper().getAddress() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.addressRequired"));
            return false;
        }
        final int startingMessagesSize = getMessages().size();
        if (ObjectUtils.isEmpty(context.getDeveloper().getAddress().getLine1())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.streetRequired"));
        }
        if (ObjectUtils.isEmpty(context.getDeveloper().getAddress().getCity())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.cityRequired"));
        }
        if (ObjectUtils.isEmpty(context.getDeveloper().getAddress().getState())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.stateRequired"));
        }
        if (ObjectUtils.isEmpty(context.getDeveloper().getAddress().getZipcode())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.zipRequired"));
        }
        return getMessages().size() <= startingMessagesSize;
    }
}
