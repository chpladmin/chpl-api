package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component
public abstract class Reviewer {

    protected ErrorMessageUtil msgUtil;

    @Autowired
    public Reviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public abstract void review(Surveillance surveillance);

    protected void addSurveillanceWarningIfNotValid(final Surveillance surv, final String input, final String fieldName) {
        if (!ValidationUtils.isValidUtf8(input)) {
            surv.getWarningMessages().add(msgUtil.getMessage("surveillance.badCharacterFound", fieldName));
        }
        if (ValidationUtils.hasNewline(input)) {
            surv.getWarningMessages().add(msgUtil.getMessage("surveillance.newlineCharacterFound", fieldName));
        }
    }
}
