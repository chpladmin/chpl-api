package gov.healthit.chpl.exception;

public class InvalidArgumentsException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidArgumentsException() {
        super();
    }

    public InvalidArgumentsException(String message) {
        super(message);
    }

    public InvalidArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidArgumentsException(Throwable cause) {
        super(cause);
    }

}
