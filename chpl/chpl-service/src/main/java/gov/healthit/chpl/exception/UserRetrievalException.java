package gov.healthit.chpl.exception;

public class UserRetrievalException extends Exception {
    private static final long serialVersionUID = 6252799316121125739L;

    public UserRetrievalException() {
        super();
    }

    public UserRetrievalException(final String message) {
        super(message);
    }

    public UserRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserRetrievalException(final Throwable cause) {
        super(cause);
    }

}
