package gov.healthit.chpl.exception;

public class EmailNotSentException extends Exception {

    private static final long serialVersionUID = -4625910508054187158L;

    public EmailNotSentException() {
        super();
    }

    public EmailNotSentException(String message) {
        super(message);
    }
}
