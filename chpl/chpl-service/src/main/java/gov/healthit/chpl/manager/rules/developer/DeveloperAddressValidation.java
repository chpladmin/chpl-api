package gov.healthit.chpl.manager.rules.developer;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperAddressValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean getErrorMessages(DeveloperValidationContext context) {
        if (context.getDeveloperDTO().getAddress() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.addressRequired"));
            return false;
        }
        final int startingMessagesSize = getMessages().size();
        if (StringUtils.isEmpty(context.getDeveloperDTO().getAddress().getStreetLineOne())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.streetRequired"));
        }
        if (StringUtils.isEmpty(context.getDeveloperDTO().getAddress().getCity())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.cityRequired"));
        }
        if (StringUtils.isEmpty(context.getDeveloperDTO().getAddress().getState())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.stateRequired"));
        }
        if (StringUtils.isEmpty(context.getDeveloperDTO().getAddress().getZipcode())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.address.zipRequired"));
        }
        return getMessages().size() <= startingMessagesSize;
    }
}
