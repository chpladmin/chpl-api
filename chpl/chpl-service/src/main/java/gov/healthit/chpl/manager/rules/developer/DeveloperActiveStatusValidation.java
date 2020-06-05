package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperActiveStatusValidation extends ValidationRule<DeveloperValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperActiveStatusValidation.class);

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        DeveloperDTO beforeDev = context.getBeforeDev();
        DeveloperStatusEventDTO currDevStatus = beforeDev.getStatus();
        if (currDevStatus != null && currDevStatus.getStatus() != null
                && !StringUtils.equals(currDevStatus.getStatus().getStatusName(), DeveloperStatusType.Active.getName())) {
            String msg = msgUtil.getMessage("developer.notActive", beforeDev.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        }
        return true;
    }
}
