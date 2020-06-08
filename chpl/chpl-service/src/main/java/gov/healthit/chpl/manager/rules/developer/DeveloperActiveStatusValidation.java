package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeveloperActiveStatusValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        DeveloperDTO developer = context.getDeveloperDTO();
        DeveloperStatusEventDTO currDevStatus = developer.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = msgUtil.getMessage("developer.noStatusFound", developer.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        } else if (currDevStatus != null && currDevStatus.getStatus() != null
                && !StringUtils.equals(currDevStatus.getStatus().getStatusName(), DeveloperStatusType.Active.getName())) {
            String msg = msgUtil.getMessage("developer.notActive", developer.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        }
        return true;
    }
}
