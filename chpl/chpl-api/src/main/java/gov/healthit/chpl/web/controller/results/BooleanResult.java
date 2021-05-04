package gov.healthit.chpl.web.controller.results;

import lombok.Data;

@Data
public class BooleanResult {
    private Boolean success;

    public BooleanResult(Boolean success) {
        this.success = success;
    }
}
