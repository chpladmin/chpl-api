package gov.healthit.chpl.auth;

public class ChplAccountEmailNotConfirmedException extends Exception {
    private static final long serialVersionUID = 8658136990867988527L;

    public ChplAccountEmailNotConfirmedException(String msg) {
        super(msg);
    }

}
