package gov.healthit.chpl.exception;

public class UserCreationException extends Exception {
    private static final long serialVersionUID = -3112621810980101181L;

    public UserCreationException() {
        super();
    }

    public UserCreationException(final String message) {
        super(message);
    }

    public UserCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserCreationException(final Throwable cause) {
        super(cause);
    }

}
