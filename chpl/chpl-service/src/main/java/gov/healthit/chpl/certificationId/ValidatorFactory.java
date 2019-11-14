package gov.healthit.chpl.certificationId;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;

@Component
public class ValidatorFactory {

    private FF4j ff4j;

    @Autowired
    public ValidatorFactory(
            final FF4j ff4j) {
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
            if (this.ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
                val = new Validator2015();
            } else {
                val = new Validator2015Legacy();
            }
        } else if ("2014/2015".equals(attYear)) {
            val = new Validator20142015();
        } else {
            val = new ValidatorDefault();
        }

        return val;
    }
}
