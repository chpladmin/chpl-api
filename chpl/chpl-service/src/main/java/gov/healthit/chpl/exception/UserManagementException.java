package gov.healthit.chpl.exception;

public class UserManagementException extends Exception {
    private static final long serialVersionUID = 7561252940887249991L;

    public UserManagementException() {
        super();
    }

    public UserManagementException(String message) {
        super(message);
    }

    public UserManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserManagementException(Throwable cause) {
        super(cause);
    }

}
