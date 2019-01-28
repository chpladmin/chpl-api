package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.util.Util;

public class SimpleCertificationId implements Serializable {
    private static final long serialVersionUID = 2521257609141032011L;
    private String certificationId;
    private Date created;

    public SimpleCertificationId() {
    }

    public SimpleCertificationId(CertificationIdDTO dto) {
        this.certificationId = dto.getCertificationId();
        this.created = dto.getCreationDate();
    }

    public String getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(final String certificationId) {
        this.certificationId = certificationId;
    }

    public Date getCreated() {
        return Util.getNewDate(created);
    }

    public void setCreated(final Date created) {
        this.created = Util.getNewDate(created);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SimpleCertificationId)) {
            return false;
        }

        SimpleCertificationId anotherId = (SimpleCertificationId) obj;
        if ((this.certificationId == null && anotherId.certificationId != null)
                || (this.certificationId != null && anotherId.certificationId == null)) {
            return false;
        }
        if ((this.created == null && anotherId.created != null)
                || (this.created != null && anotherId.created == null)) {
            return false;
        }
        return this.certificationId.equals(anotherId.certificationId) && this.created.equals(anotherId.created);
    }

    @Override
    public int hashCode() {
        return this.certificationId.hashCode() + this.created.hashCode();
    }
}
