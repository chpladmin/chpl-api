package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
    private Date submittedDate;

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

    public Date getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(final Date submittedDate) {
        this.submittedDate = submittedDate;
    }

    @Override
    public String toString() {
        return "ChangeRequest [id=" + id + ", changeRequestType=" + changeRequestType + ", developer=" + developer
                + ", certificationBodies=" + certificationBodies + ", currentStatus=" + currentStatus + ", statuses="
                + statuses + ", details=" + details + ", submittedDate=" + submittedDate + "]";
    }
}
