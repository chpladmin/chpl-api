package gov.healthit.chpl.entity.listing;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.report.PrivilegedSurveillanceEntity;
import gov.healthit.chpl.util.Util;

/**
 * Entity containing entirety of a Certified Product.
 * @author alarned
 *
 */
@Entity
@Immutable
@Table(name = "certified_product_details")
public class ListingWithPrivilegedSurveillanceEntity {
    @Id
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @Column(name = "certification_status_name")
    private String certificationStatusName;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Column(name = "year")
    private String year;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "certification_body_code")
    private String certificationBodyCode;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certifiedProductId")
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<PrivilegedSurveillanceEntity> surveillances = new HashSet<PrivilegedSurveillanceEntity>();

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public Long getCertificationStatusId() {
        return certificationStatusId;
    }

    public void setCertificationStatusId(final Long certificationStatusId) {
        this.certificationStatusId = certificationStatusId;
    }

    public String getCertificationStatusName() {
        return certificationStatusName;
    }

    public void setCertificationStatusName(final String certificationStatusName) {
        this.certificationStatusName = certificationStatusName;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public String getCertificationBodyName() {
        return certificationBodyName;
    }

    public void setCertificationBodyName(final String certificationBodyName) {
        this.certificationBodyName = certificationBodyName;
    }

    public String getCertificationBodyCode() {
        return certificationBodyCode;
    }

    public void setCertificationBodyCode(final String certificationBodyCode) {
        this.certificationBodyCode = certificationBodyCode;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Set<PrivilegedSurveillanceEntity> getSurveillances() {
        return surveillances;
    }

    public void setSurveillances(final Set<PrivilegedSurveillanceEntity> surveillances) {
        this.surveillances = surveillances;
    }

    public Date getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = certificationDate;
    }
}
