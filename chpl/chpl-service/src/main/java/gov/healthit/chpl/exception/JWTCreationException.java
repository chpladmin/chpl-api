package gov.healthit.chpl.exception;

public class JWTCreationException extends Exception {
    private static final long serialVersionUID = 4848545813842324671L;

    public JWTCreationException() {
        super();
    }

    public JWTCreationException(final String message) {
        super(message);
    }

    public JWTCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JWTCreationException(final Throwable cause) {
        super(cause);
    }

}
