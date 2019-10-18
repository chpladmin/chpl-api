package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.auth.UserPermission;

public class ChangeRequestStatus implements Serializable {
    private static final long serialVersionUID = 3333749014276324377L;

    private Long id;
    private ChangeRequestStatusType changeRequestStatusType;
    private Date statusChangeDate;
    private String comment;
    private CertificationBody certificationBody;
    private UserPermission userPermission;

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

    public String getComment() {
        return comment;
    }

    public void setComment(final String commment) {
        this.comment = commment;
    }

    public CertificationBody getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBody certificationBody) {
        this.certificationBody = certificationBody;
    }

    public UserPermission getUserPermission() {
        return userPermission;
    }

    public void setUserPermission(UserPermission userPermission) {
        this.userPermission = userPermission;
    }
}
