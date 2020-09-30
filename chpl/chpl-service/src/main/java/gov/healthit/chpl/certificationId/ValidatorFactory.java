package gov.healthit.chpl.certificationId;

import org.springframework.stereotype.Component;

@Component
public class ValidatorFactory {
    /**
     * Retrieve the validator that should be used for a given year.
     * @param attYear the given year
     * @return the appropriate validator
     */
    public Validator getValidator(final String attYear) {
        Validator val = null;

         if ("2015".equals(attYear)) {
            val = new Validator2015();
        } else {
            val = new ValidatorDefault();
        }

        return val;
    }
}
