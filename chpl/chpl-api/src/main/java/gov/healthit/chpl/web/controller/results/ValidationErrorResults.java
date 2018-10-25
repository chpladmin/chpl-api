package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResults implements Serializable {
    private static final long serialVersionUID = 6008508544263971580L;
    private List<String> errors;

    public ValidationErrorResults() {
        errors = new ArrayList<String>();
    }

    public ValidationErrorResults(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getValidationErrors() {
        return errors;
    }

    public void setValidationErrors(List<String> errors) {
        this.errors = errors;
    }

}
