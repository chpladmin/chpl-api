package gov.healthit.chpl.exception;

import java.util.ArrayList;
import java.util.List;

public class ObjectsMissingValidationException extends Exception {
    private static final long serialVersionUID = -6542978782655873229L;

    private List<ObjectMissingValidationException> exceptions;

    public ObjectsMissingValidationException() {
        super();
        exceptions = new ArrayList<ObjectMissingValidationException>();
    }

    public List<ObjectMissingValidationException> getExceptions() {
        return exceptions;
    }

    public void setExceptions(final List<ObjectMissingValidationException> exceptions) {
        this.exceptions = exceptions;
    }

}
