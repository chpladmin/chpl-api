package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component("surveillanceUnsupportedCharacterReviewer")
public class UnsupportedCharacterReviewer extends Reviewer {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UnsupportedCharacterReviewer(ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(Surveillance surv) {
        if (surv.getType() != null) {
            addSurveillanceWarningForInvalidCharacters(surv, surv.getType().getName(), "Surveillance Type");
        }

        for (SurveillanceRequirement req : surv.getRequirements()) {
            addSurveillanceWarningForInvalidCharacters(surv, req.getRequirement(),
                    "Requirement '" + req.getRequirement() + "'");
            for (SurveillanceNonconformity nc : req.getNonconformities()) {
                addSurveillanceWarningForInvalidCharacters(surv, nc.getDeveloperExplanation(),
                        "Developer Explanation '" + nc.getDeveloperExplanation() + "'");
                addSurveillanceWarningForInvalidCharacters(surv, nc.getFindings(), "Findings '" + nc.getFindings() + "'");
                addSurveillanceWarningForInvalidCharacters(surv, nc.getNonconformityType(),
                        "Nonconformity Type '" + nc.getNonconformityType() + "'");
                addSurveillanceWarningForInvalidCharacters(surv, nc.getResolution(),
                        "Resolution '" + nc.getResolution() + "'");
                addSurveillanceWarningForInvalidCharacters(surv, nc.getSummary(), "Summary '" + nc.getSummary() + "'");
            }
        }
    }

    private void addSurveillanceWarningForInvalidCharacters(Surveillance surv, String input, String fieldName) {
        if (!validationUtils.isValidUtf8(input)) {
            surv.getWarningMessages().add(msgUtil.getMessage("surveillance.badCharacterFound", fieldName));
        }
        if (validationUtils.hasNewline(input)) {
            surv.getWarningMessages().add(msgUtil.getMessage("surveillance.newlineCharacterFound", fieldName));
        }
    }
}
