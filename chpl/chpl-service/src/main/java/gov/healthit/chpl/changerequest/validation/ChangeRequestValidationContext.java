package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.DeveloperDAO;

public class ChangeRequestValidationContext {
    private ChangeRequest changeRequest;
    private ChangeRequestTypeDAO changeRequestTypeDAO;
    private DeveloperDAO developerDAO;

    public ChangeRequestValidationContext(final ChangeRequest changeRequest,
            final ChangeRequestTypeDAO changeRequestTypeDAO, final DeveloperDAO developerDAO) {
        this.changeRequest = changeRequest;
        this.changeRequestTypeDAO = changeRequestTypeDAO;
        this.developerDAO = developerDAO;
    }

    public ChangeRequest getChangeRequest() {
        return changeRequest;
    }

    public void setChangeRequest(ChangeRequest changeRequest) {
        this.changeRequest = changeRequest;
    }

    public ChangeRequestTypeDAO getChangeRequestTypeDAO() {
        return changeRequestTypeDAO;
    }

    public void setChangeRequestTypeDAO(ChangeRequestTypeDAO changeRequestTypeDAO) {
        this.changeRequestTypeDAO = changeRequestTypeDAO;
    }

    public DeveloperDAO getDeveloperDAO() {
        return developerDAO;
    }

    public void setDeveloperDAO(DeveloperDAO developerDAO) {
        this.developerDAO = developerDAO;
    }

}
