package gov.healthit.chpl.exception;

public class UserAccountExistsException extends Exception {
    private static final long serialVersionUID = 6252445316761125739L;

    public UserAccountExistsException() {
        super();
    }

    public UserAccountExistsException(String message) {
        super(message);
    }

    public UserAccountExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAccountExistsException(Throwable cause) {
        super(cause);
    }

}
