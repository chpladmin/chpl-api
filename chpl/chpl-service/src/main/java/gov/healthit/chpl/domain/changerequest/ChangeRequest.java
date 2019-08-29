package gov.healthit.chpl.domain.changerequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;

public class ChangeRequest implements Serializable {
    private static final long serialVersionUID = 216843913133697622L;

    private Long id;
    private ChangeRequestType changeRequestType;
    private Developer developer;
    private List<CertificationBody> certificationBodies = new ArrayList<CertificationBody>();
    private ChangeRequestStatus currentStatus;
    private List<ChangeRequestStatus> statuses = new ArrayList<ChangeRequestStatus>();
    private Object details;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ChangeRequestType getChangeRequestType() {
        return changeRequestType;
    }

    public void setChangeRequestType(final ChangeRequestType changeRequestType) {
        this.changeRequestType = changeRequestType;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

    public List<CertificationBody> getCertificationBodies() {
        return certificationBodies;
    }

    public void setCertificationBodies(final List<CertificationBody> certificationBodies) {
        this.certificationBodies = certificationBodies;
    }

    public ChangeRequestStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(final ChangeRequestStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public List<ChangeRequestStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(final List<ChangeRequestStatus> statuses) {
        this.statuses = statuses;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(final Object details) {
        this.details = details;
    }
}
