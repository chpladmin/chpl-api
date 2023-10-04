package gov.healthit.chpl.certificationId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class ValidatorFactory {

    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public ValidatorFactory(CertificationCriterionService certificationCriterionService) {
        this.certificationCriterionService = certificationCriterionService;
    }

    /**
     * Retrieve the validator that should be used for a given year.
     * @param attYear the given year
     * @return the appropriate validator
     */
    public Validator getValidator(final String attYear) {
        Validator val = null;

        if ("2014".equals(attYear)) {
            val = new Validator2014();
        } else if ("2015".equals(attYear)) {
            val = new Validator2015(certificationCriterionService);
        } else if ("2014/2015".equals(attYear)) {
            val = new Validator20142015();
        } else {
            val = new ValidatorDefault();
        }

        return val;
    }
}
