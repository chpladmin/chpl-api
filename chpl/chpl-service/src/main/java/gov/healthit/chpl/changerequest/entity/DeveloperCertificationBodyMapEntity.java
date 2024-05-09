package gov.healthit.chpl.changerequest.entity;

import java.io.Serializable;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Immutable
@Getter
@Setter
@ToString
@Table(name = "developer_certification_body_map")
public class DeveloperCertificationBodyMapEntity {

    @EmbeddedId
    private DeveloperCertificationBodyMapId id;

    @Basic(optional = false)
    @Column(name = "vendor_id", insertable = false, updatable = false)
    private Long developerId;

    @Basic(optional = false)
    @Column(name = "certification_body_id", insertable = false, updatable = false)
    private Long certificationBodyId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false, insertable = false, updatable = false)
    private DeveloperEntity developer;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", nullable = false, insertable = false, updatable = false)
    private CertificationBodyEntity certificationBody;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeveloperCertificationBodyMapId other = (DeveloperCertificationBodyMapId) obj;
        if (certificationBodyId == null) {
            if (other.certificationBodyId != null) {
                return false;
            }
        } else if (!certificationBodyId.equals(other.certificationBodyId)) {
            return false;
        }
        if (developerId == null) {
            if (other.developerId != null) {
                return false;
            }
        } else if (!developerId.equals(other.developerId)) {
            return false;
        }
        return true;
    }

}
