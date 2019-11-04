package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;

public class ChangeRequestValidationContext {
    private ChangeRequest changeRequest;
    private ChangeRequest crFromDb;

    public ChangeRequestValidationContext(final ChangeRequest changeRequest, final ChangeRequest crFromDb) {
        this.changeRequest = changeRequest;
        this.crFromDb = crFromDb;
    }

    public ChangeRequest getChangeRequest() {
        return changeRequest;
    }

    public void setChangeRequest(ChangeRequest changeRequest) {
        this.changeRequest = changeRequest;
    }

    public ChangeRequest getCrFromDb() {
        return crFromDb;
    }

    public void setCrFromDb(ChangeRequest changeRequest) {
        this.crFromDb = changeRequest;
    }
}
