package gov.healthit.chpl.exception;

public class UserPermissionRetrievalException extends Exception {
    private static final long serialVersionUID = 8037775842051693445L;

    public UserPermissionRetrievalException() {
        super();
    }

    public UserPermissionRetrievalException(final String message) {
        super(message);
    }

    public UserPermissionRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserPermissionRetrievalException(final Throwable cause) {
        super(cause);
    }

}
