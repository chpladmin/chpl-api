package gov.healthit.chpl.exception;

public class CertifiedProductUpdateException extends Exception {
    private static final long serialVersionUID = 885150958308195721L;

    public CertifiedProductUpdateException() {
        super();
    }

    public CertifiedProductUpdateException(String message) {
        super(message);
    }

    public CertifiedProductUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertifiedProductUpdateException(Throwable cause) {
        super(cause);
    }
}
