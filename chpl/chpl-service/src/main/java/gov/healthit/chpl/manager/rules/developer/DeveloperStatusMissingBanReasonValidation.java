package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
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
        Developer updatedDev = context.getDeveloper();
        for (DeveloperStatusEvent statusEvent : updatedDev.getStatusEvents()) {
            if (statusEvent.getStatus().getStatus()
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
