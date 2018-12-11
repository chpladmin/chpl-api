package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "ehr_certification_ids_and_products")
public class CertificationIdAndCertifiedProductEntity implements Serializable {
    private static final long serialVersionUID = -1L;

    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "ehr_certification_id", nullable = false)
    private Long ehrCertificationId;

    @Basic(optional = false)
    @Column(name = "ehr_certification_id_text", length = 255, nullable = false)
    private String certificationId;

    @Basic(optional = false)
    @Column(name = "ehr_certification_id_creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "chpl_product_number")
    private String legacyChplNumber;

    @Column(name = "year")
    private String certificationYear;

    @Column(name = "testing_lab_code")
    private String atlCode;

    @Column(name = "certification_body_code")
    private String acbCode;

    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "version_code")
    private String versionCode;

    @Column(name = "ics_code")
    private String icsCode;

    @Column(name = "additional_software_code")
    private String additionalSoftwareCode;

    @Column(name = "certified_date_code")
    private String certifiedDateCode;

    public CertificationIdAndCertifiedProductEntity() {
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Long getId() {
        return this.id;

    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getEhrCertificationId() {
        return ehrCertificationId;
    }

    public void setEhrCertificationId(final Long ehrCertificationId) {
        this.ehrCertificationId = ehrCertificationId;
    }

    public String getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(final String certificationId) {
        this.certificationId = certificationId;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public String getLegacyChplNumber() {
        return legacyChplNumber;
    }

    public void setLegacyChplNumber(final String legacyChplNumber) {
        this.legacyChplNumber = legacyChplNumber;
    }

    public String getCertificationYear() {
        return certificationYear;
    }

    public void setCertificationYear(final String certificationYear) {
        this.certificationYear = certificationYear;
    }

    public String getAtlCode() {
        return atlCode;
    }

    public void setAtlCode(final String atlCode) {
        this.atlCode = atlCode;
    }

    public String getAcbCode() {
        return acbCode;
    }

    public void setAcbCode(final String acbCode) {
        this.acbCode = acbCode;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(final String developerCode) {
        this.developerCode = developerCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(final String productCode) {
        this.productCode = productCode;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(final String versionCode) {
        this.versionCode = versionCode;
    }

    public String getIcsCode() {
        return icsCode;
    }

    public void setIcsCode(final String icsCode) {
        this.icsCode = icsCode;
    }

    public String getAdditionalSoftwareCode() {
        return additionalSoftwareCode;
    }

    public void setAdditionalSoftwareCode(final String additionalSoftwareCode) {
        this.additionalSoftwareCode = additionalSoftwareCode;
    }

    public String getCertifiedDateCode() {
        return certifiedDateCode;
    }

    public void setCertifiedDateCode(final String certifiedDateCode) {
        this.certifiedDateCode = certifiedDateCode;
    }

}
