package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperStatusMissingBanReasonValidation extends ValidationRule<DeveloperValidationContext> {
    /**
     * If any of the statuses (new, old, or any other status in the history) is
     * Under Certification Ban by ONC make sure there is a reason given
     */
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        DeveloperDTO updatedDev = context.getDeveloperDTO();
        for (DeveloperStatusEventDTO statusEvent : updatedDev.getStatusEvents()) {
            if (statusEvent.getStatus().getStatusName()
                    .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                    && StringUtils.isEmpty(statusEvent.getReason())) {
                getMessages().add(msgUtil.getMessage("developer.missingReasonForBan",
                        DeveloperStatusType.UnderCertificationBanByOnc.toString()));
                return false;
            }
        }
        return true;
    }
}
