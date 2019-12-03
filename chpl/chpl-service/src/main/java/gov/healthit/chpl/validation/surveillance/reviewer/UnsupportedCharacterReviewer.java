package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class UnsupportedCharacterReviewer extends Reviewer {

    @Autowired
    public UnsupportedCharacterReviewer(ErrorMessageUtil msgUtil) {
        super(msgUtil);
    }

    public void review(Surveillance surv) {
        if (surv.getType() != null) {
            addSurveillanceWarningIfNotValid(surv, surv.getType().getName(), "Surveillance Type");
        }
    }
}
