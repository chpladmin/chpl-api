package gov.healthit.chpl.certificationId;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class ValidatorFactory {

    private CertificationCriterionService certificationCriterionService;
    private FF4j ff4j;

    @Autowired
    public ValidatorFactory(CertificationCriterionService certificationCriterionService,
            FF4j ff4j) {
        this.certificationCriterionService = certificationCriterionService;
        this.ff4j = ff4j;
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
            if (this.ff4j.check(FeatureList.ERD_PHASE_2)) {
                val = new Validator2015(certificationCriterionService);
            } else {
                val = new Validator2015Legacy(certificationCriterionService);
            }
        } else if ("2014/2015".equals(attYear)) {
            val = new Validator20142015();
        } else {
            val = new ValidatorDefault();
        }

        return val;
    }
}
