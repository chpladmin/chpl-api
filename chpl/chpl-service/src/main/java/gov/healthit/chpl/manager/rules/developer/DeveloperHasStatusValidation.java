package gov.healthit.chpl.manager.rules.developer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperHasStatusValidation extends ValidationRule<DeveloperValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperHasStatusValidation.class);

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        DeveloperDTO beforeDev = context.getBeforeDev();
        DeveloperStatusEventDTO currDevStatus = beforeDev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = msgUtil.getMessage("developer.noStatusFound", beforeDev.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        }
        return true;
    }
}
