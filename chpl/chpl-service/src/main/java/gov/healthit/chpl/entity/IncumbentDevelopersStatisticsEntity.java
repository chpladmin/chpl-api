package gov.healthit.chpl.entity;

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
import javax.persistence.Transient;

import gov.healthit.chpl.util.Util;

/**
 * Entity object representing the incumbent_developers_statistics table.
 * @author alarned
 *
 */
@Entity
@Table(name = "incumbent_developers_statistics")
public class IncumbentDevelopersStatisticsEntity implements Serializable {
    private static final long serialVersionUID = 1313677047965534572L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "new_count", nullable = false)
    private Long newCount;

    @Basic(optional = false)
    @Column(name = "incumbent_count", nullable = false)
    private Long incumbentCount;

    @Basic(optional = false)
    @Column(name = "old_certification_edition_id", nullable = false)
    private Long oldCertificationEditionId;

    @Basic(optional = false)
    @Column(name = "new_certification_edition_id", nullable = false)
    private Long newCertificationEditionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "old_certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity oldCertificationEdition;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "new_certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity newCertificationEdition;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public IncumbentDevelopersStatisticsEntity() {
        this.newCount = 0L;
        this.incumbentCount = 0L;
    }

    /**
     * Sets the id field upon creation.
     * @param id The value to set object's id equal to
     */
    public IncumbentDevelopersStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return IncumbentDevelopersStatisticsEntity.class;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getNewCount() {
        return newCount;
    }

    public void setNewCount(final Long newCount) {
        this.newCount = newCount;
    }

    public Long getIncumbentCount() {
        return incumbentCount;
    }

    public void setIncumbentCount(final Long incumbentCount) {
        this.incumbentCount = incumbentCount;
    }

    public Long getOldCertificationEditionId() {
        return oldCertificationEditionId;
    }

    public void setOldCertificationEditionId(final Long oldCertificationEditionId) {
        this.oldCertificationEditionId = oldCertificationEditionId;
    }

    public Long getNewCertificationEditionId() {
        return newCertificationEditionId;
    }

    public void setNewCertificationEditionId(final Long newCertificationEditionId) {
        this.newCertificationEditionId = newCertificationEditionId;
    }

    public CertificationEditionEntity getOldCertificationEdition() {
        return oldCertificationEdition;
    }

    public void setOldCertificationEdition(final CertificationEditionEntity oldCertificationEdition) {
        this.oldCertificationEdition = oldCertificationEdition;
    }

    public CertificationEditionEntity getNewCertificationEdition() {
        return newCertificationEdition;
    }

    public void setNewCertificationEdition(final CertificationEditionEntity newCertificationEdition) {
        this.newCertificationEdition = newCertificationEdition;
    }

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

    @Override
    public String toString() {
        return "Incumbent Developers Statistics Entity ["
                + "[New: " + this.newCount + "]"
                + "[Incumbent: " + this.incumbentCount + "]"
                + "[Old Edition: " + this.oldCertificationEditionId.toString() + "]"
                + "[New Edition: " + this.newCertificationEditionId.toString() + "]"
                + "]";
    }
}
