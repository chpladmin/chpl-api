package gov.healthit.chpl.certificationId;

public abstract class ValidatorFactory {
    public static Validator getValidator(String attYear) {
        Validator val = null;

        if ("2014".equals(attYear)) {
            val = new Validator2014();
        } else if ("2015".equals(attYear)) {
            val = new Validator2015Legacy();
        } else if ("2014/2015".equals(attYear)) {
            val = new Validator20142015();
        } else {
            val = new ValidatorDefault();
        }

        return val;
    }
}
