package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.DeveloperDAO;

public class ChangeRequestValidationContext {
    private ChangeRequest changeRequest;
    private ChangeRequest crFromDb;
    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestTypeDAO changeRequestTypeDAO;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private DeveloperDAO developerDAO;

    public ChangeRequestValidationContext(final ChangeRequest changeRequest, final ChangeRequest crFromDb,
            final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestTypeDAO changeRequestTypeDAO, ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO,
            final DeveloperDAO developerDAO) {
        this.changeRequest = changeRequest;
        this.crFromDb = crFromDb;
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestTypeDAO = changeRequestTypeDAO;
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.developerDAO = developerDAO;
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

    public ChangeRequestDAO getChangeRequestDAO() {
        return changeRequestDAO;
    }

    public void setChangeRequestDAO(ChangeRequestDAO changeRequestDAO) {
        this.changeRequestDAO = changeRequestDAO;
    }

    public ChangeRequestStatusTypeDAO getChangeRequestStatusTypeDAO() {
        return changeRequestStatusTypeDAO;
    }

    public void setChangeRequestStatusTypeDAO(ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO) {
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
    }

}
