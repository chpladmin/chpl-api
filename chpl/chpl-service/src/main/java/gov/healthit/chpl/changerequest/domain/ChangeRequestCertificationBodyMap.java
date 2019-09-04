package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;

public class ChangeRequestCertificationBodyMap implements Serializable {
    private static final long serialVersionUID = -8254881061266219280L;

    private Long id;
    private ChangeRequest changeRequest;
    private CertificationBody certificationBody;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ChangeRequest getChangeRequest() {
        return changeRequest;
    }

    public void setChangeRequest(final ChangeRequest changeRequest) {
        this.changeRequest = changeRequest;
    }

    public CertificationBody getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBody certificationBody) {
        this.certificationBody = certificationBody;
    }

}
