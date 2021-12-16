package gov.healthit.chpl.exception;

public class ObjectNotFoundException extends Exception  {
    private static final long serialVersionUID = -4894472154120923759L;

    public ObjectNotFoundException() {
        super();
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }
}
