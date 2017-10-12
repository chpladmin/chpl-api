package gov.healthit.chpl.dao;

public class EntityRetrievalException extends Exception {

    private static final long serialVersionUID = 1L;

    public EntityRetrievalException() {
        super();
    }

    public EntityRetrievalException(String message) {
        super(message);
    }

    public EntityRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityRetrievalException(Throwable cause) {
        super(cause);
    }

}
