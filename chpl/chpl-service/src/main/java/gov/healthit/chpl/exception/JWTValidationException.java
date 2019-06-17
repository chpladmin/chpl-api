package gov.healthit.chpl.exception;

public class JWTValidationException extends Exception {
    private static final long serialVersionUID = -6523694263924298813L;

    public JWTValidationException() {
        super();
    }

    public JWTValidationException(final String message) {
        super(message);
    }

    public JWTValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JWTValidationException(final Throwable cause) {
        super(cause);
    }

}
