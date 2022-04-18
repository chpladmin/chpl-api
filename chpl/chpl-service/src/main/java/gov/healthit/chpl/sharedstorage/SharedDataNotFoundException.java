package gov.healthit.chpl.sharedstorage;

public class SharedDataNotFoundException extends Exception {
    private static final long serialVersionUID = 4054030238422080881L;

    public SharedDataNotFoundException() {
        super();
    }

    public SharedDataNotFoundException(String message) {
        super(message);
    }

    public SharedDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SharedDataNotFoundException(Throwable cause) {
        super(cause);
    }

}
