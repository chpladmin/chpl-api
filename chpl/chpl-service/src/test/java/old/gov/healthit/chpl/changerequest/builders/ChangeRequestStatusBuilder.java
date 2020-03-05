package old.gov.healthit.chpl.changerequest.builders;

import java.util.Date;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.domain.CertificationBody;

public class ChangeRequestStatusBuilder {
    private Long id;
    private ChangeRequestStatusType changeRequestStatusType;
    private Date statusChangeDate;
    private String comment;
    private CertificationBody certificationBody;

    public ChangeRequestStatusBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ChangeRequestStatusBuilder withChangeRequestStatusType(ChangeRequestStatusType changeRequestStatusType) {
        this.changeRequestStatusType = changeRequestStatusType;
        return this;
    }

    public ChangeRequestStatusBuilder withStatusChangeDate(Date statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
        return this;
    }

    public ChangeRequestStatusBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public ChangeRequestStatusBuilder withCertificationBody(CertificationBody acb) {
        this.certificationBody = acb;
        return this;
    }

    public ChangeRequestStatus build() {
        ChangeRequestStatus status = new ChangeRequestStatus();
        status.setId(id);
        status.setChangeRequestStatusType(changeRequestStatusType);
        status.setStatusChangeDate(statusChangeDate);
        status.setComment(comment);
        status.setCertificationBody(certificationBody);
        return status;
    }
}
