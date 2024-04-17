package gov.healthit.chpl.exception;

public class ActivityException extends Exception {
    private static final long serialVersionUID = -8130262303253266578L;

    public ActivityException() {
        super();
    }

    public ActivityException(String message) {
        super(message);
    }

    public ActivityException(Throwable exception) {
        super(exception);
    }
}
