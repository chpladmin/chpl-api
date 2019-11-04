package gov.healthit.chpl.changerequest.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;

@Entity
@Table(name = "developer_certification_body_map")
public class DeveloperCertificationBodyMapEntity {

    @EmbeddedId
    private DeveloperCertificationBodyMapId id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false, insertable = false,
            updatable = false)
    private DeveloperEntity developer;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", nullable = false, insertable = false,
            updatable = false)
    private CertificationBodyEntity certificationBody;

    public DeveloperCertificationBodyMapId getId() {
        return id;
    }

    public void setId(final DeveloperCertificationBodyMapId id) {
        this.id = id;
    }

    public DeveloperEntity getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperEntity developer) {
        this.developer = developer;
    }

    public CertificationBodyEntity getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBodyEntity certificationBody) {
        this.certificationBody = certificationBody;
    }
}

@Embeddable
class DeveloperCertificationBodyMapId implements Serializable {
    private static final long serialVersionUID = 3779022648713498861L;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "vendor_id")
    private Long developerId;

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((certificationBodyId == null) ? 0 : certificationBodyId.hashCode());
        result = prime * result + ((developerId == null) ? 0 : developerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeveloperCertificationBodyMapId other = (DeveloperCertificationBodyMapId) obj;
        if (certificationBodyId == null) {
            if (other.certificationBodyId != null)
                return false;
        } else if (!certificationBodyId.equals(other.certificationBodyId))
            return false;
        if (developerId == null) {
            if (other.developerId != null)
                return false;
        } else if (!developerId.equals(other.developerId))
            return false;
        return true;
    }

}
