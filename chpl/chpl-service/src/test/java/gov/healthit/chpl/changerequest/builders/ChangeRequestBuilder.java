package gov.healthit.chpl.changerequest.builders;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;

public class ChangeRequestBuilder {

    private Long id;
    private ChangeRequestType changeRequestType;
    private Developer developer;
    private List<CertificationBody> certificationBodies = new ArrayList<CertificationBody>();
    private ChangeRequestStatus currentStatus;
    private List<ChangeRequestStatus> statuses = new ArrayList<ChangeRequestStatus>();
    private Object details;

    public ChangeRequestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ChangeRequestBuilder withChangeRequestType(ChangeRequestType changeRequestType) {
        this.changeRequestType = changeRequestType;
        return this;
    }

    public ChangeRequestBuilder withDeveloper(Developer developer) {
        this.developer = developer;
        return this;
    }

    public ChangeRequestBuilder withCurrentStatus(ChangeRequestStatus changeRequestStatus) {
        this.currentStatus = changeRequestStatus;
        return this;
    }

    public ChangeRequestBuilder withDetails(Object details) {
        this.details = details;
        return this;
    }

    public ChangeRequestBuilder addCertificationBody(CertificationBody certificationBody) {
        this.certificationBodies.add(certificationBody);
        return this;
    }

    public ChangeRequestBuilder addChangeRequestStatus(ChangeRequestStatus crStatus) {
        this.statuses.add(crStatus);
        return this;
    }

    public ChangeRequest build() {
        ChangeRequest cr = new ChangeRequest();
        cr.setId(id);
        cr.setChangeRequestType(changeRequestType);
        cr.setDeveloper(developer);
        cr.setCurrentStatus(currentStatus);
        cr.setDetails(details);
        cr.setCertificationBodies(certificationBodies);
        cr.setStatuses(statuses);
        return cr;
    }
}
