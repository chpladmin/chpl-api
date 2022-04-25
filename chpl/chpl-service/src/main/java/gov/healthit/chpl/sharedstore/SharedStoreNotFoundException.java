package gov.healthit.chpl.sharedstore;

public class SharedStoreNotFoundException extends Exception {
    private static final long serialVersionUID = 4054030238422080881L;

    public SharedStoreNotFoundException() {
        super();
    }

    public SharedStoreNotFoundException(String message) {
        super(message);
    }

    public SharedStoreNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SharedStoreNotFoundException(Throwable cause) {
        super(cause);
    }

}
