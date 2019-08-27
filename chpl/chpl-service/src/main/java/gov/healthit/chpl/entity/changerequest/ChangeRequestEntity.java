package gov.healthit.chpl.entity.changerequest;

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
import javax.validation.constraints.NotNull;

import gov.healthit.chpl.entity.developer.DeveloperEntity;

@Entity
@Table(name = "change_request")
public class ChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_type_id", nullable = false, insertable = true,
            updatable = false)
    private ChangeRequestTypeEntity changeRequestType;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false, insertable = true,
            updatable = false)
    private DeveloperEntity developer;

    /*
     * @Basic(optional = true)
     * 
     * @JoinTable(name = "change_request_certification_body_map", joinColumns =
     * {
     * 
     * @JoinColumn(name = "change_request_id", referencedColumnName = "id") },
     * inverseJoinColumns = {
     * 
     * @JoinColumn(name = "certification_body_id", referencedColumnName =
     * "certification_body_id") }) private List<CertificationBodyEntity>
     * certificationBodies;
     */

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @NotNull()
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @NotNull()
    @Column(nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ChangeRequestTypeEntity getChangeRequestType() {
        return changeRequestType;
    }

    public void setChangeRequestType(final ChangeRequestTypeEntity changeRequestType) {
        this.changeRequestType = changeRequestType;
    }

    public DeveloperEntity getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperEntity developer) {
        this.developer = developer;
    }

    /*
     * public List<CertificationBodyEntity> getCertificationBodies() { return
     * certificationBodies; }
     * 
     * public void setCertificationBodies(final List<CertificationBodyEntity>
     * certificationBodies) { this.certificationBodies = certificationBodies; }
     */

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

}
