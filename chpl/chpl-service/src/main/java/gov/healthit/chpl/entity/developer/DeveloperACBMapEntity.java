package gov.healthit.chpl.entity.developer;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "acb_vendor_map")
public class DeveloperACBMapEntity implements Serializable {

    private static final long serialVersionUID = 555395798107190947L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "acb_vendor_map_id")
    private Long id;

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", unique = true, nullable = true, insertable = false, updatable = false)
    private CertificationBodyEntity certificationBody;

    @Column(name = "transparency_attestation")
    @Type(type = "gov.healthit.chpl.entity.PostgresAttestationType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "gov.healthit.chpl.entity.AttestationType")
    })
    private AttestationType transparencyAttestation;

    public DeveloperACBMapEntity() {
        // Default constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void getDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public AttestationType getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(final AttestationType transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    protected Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    protected Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    protected Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public CertificationBodyEntity getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBodyEntity certificationBody) {
        this.certificationBody = certificationBody;
    }
}
