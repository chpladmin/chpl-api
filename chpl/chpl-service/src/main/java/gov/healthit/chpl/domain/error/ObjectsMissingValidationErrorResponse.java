package gov.healthit.chpl.domain.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectsMissingValidationErrorResponse implements Serializable {
    private static final long serialVersionUID = -2186343673031903254L;
    private List<ObjectMissingValidationErrorResponse> errors;

    public ObjectsMissingValidationErrorResponse() {
        errors = new ArrayList<ObjectMissingValidationErrorResponse>();
    }

    public List<ObjectMissingValidationErrorResponse> getErrors() {
        return errors;
    }

    public void setErrors(final List<ObjectMissingValidationErrorResponse> errors) {
        this.errors = errors;
    }
}
