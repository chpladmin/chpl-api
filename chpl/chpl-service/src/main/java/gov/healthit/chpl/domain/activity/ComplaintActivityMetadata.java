package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.domain.CertificationBody;

public class ComplaintActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3877401017233923058L;

    private CertificationBody certificationBody;

    public CertificationBody getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(CertificationBody certificationBody) {
        this.certificationBody = certificationBody;
    }

}
