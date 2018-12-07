package gov.healthit.chpl.exception;

public class MissingReasonException extends Exception {
    private static final long serialVersionUID = -5802455787158297215L;

    public MissingReasonException() {
        super();
    }

    public MissingReasonException(String message) {
        super(message);
    }
}
