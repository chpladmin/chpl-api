package gov.healthit.chpl.domain.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectsMissingValidationErrorJSONObject implements Serializable {
    private static final long serialVersionUID = -2186343673031903254L;
    private List<ObjectMissingValidationErrorJSONObject> errors;

    public ObjectsMissingValidationErrorJSONObject() {
        errors = new ArrayList<ObjectMissingValidationErrorJSONObject>();
    }

    public List<ObjectMissingValidationErrorJSONObject> getErrors() {
        return errors;
    }

    public void setErrors(final List<ObjectMissingValidationErrorJSONObject> errors) {
        this.errors = errors;
    }
}
