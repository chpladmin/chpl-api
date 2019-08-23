package gov.healthit.chpl.domain.changerequest;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationBody;

public class ChangeRequestStatus implements Serializable {
    private static final long serialVersionUID = 3333749014276324377L;

    private Long id;
    private ChangeRequestStatusType changeRequestStatusType;
    private Date statusChangeDate;
    private String commment;
    private CertificationBody certificationBody;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ChangeRequestStatusType getChangeRequestStatusType() {
        return changeRequestStatusType;
    }

    public void setChangeRequestStatusType(final ChangeRequestStatusType changeRequestStatusType) {
        this.changeRequestStatusType = changeRequestStatusType;
    }

    public Date getStatusChangeDate() {
        return statusChangeDate;
    }

    public void setStatusChangeDate(final Date statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
    }

    public String getCommment() {
        return commment;
    }

    public void setCommment(final String commment) {
        this.commment = commment;
    }

    public CertificationBody getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBody certificationBody) {
        this.certificationBody = certificationBody;
    }
}
