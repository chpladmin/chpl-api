package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class RandomizedSurveillanceReviewer extends Reviewer {

    @Autowired
    public RandomizedSurveillanceReviewer(ErrorMessageUtil msgUtil) {
        super(msgUtil);
    }

    public void review(Surveillance surv) {
        // randomized surveillance requires number of sites used but
        // any other type of surveillance should not have that value
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (surv.getRandomizedSitesUsed() == null || surv.getRandomizedSitesUsed().intValue() < 0) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.randomizedNonzeroValue"));
            }
        } else if (surv.getType() != null && surv.getType().getName() != null
                && !surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (surv.getRandomizedSitesUsed() != null && surv.getRandomizedSitesUsed().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.randomizedSitesNotApplicable", surv.getType().getName()));
            }
        }
    }
}
