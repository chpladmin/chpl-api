package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeveloperNotBannedOrSuspendedValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        Developer developer = context.getDeveloper();
        if (!developer.isNotBannedOrSuspended()) {
            String msg = msgUtil.getMessage("developer.notBannedOrSuspended", developer.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        }
        return true;
    }
}
