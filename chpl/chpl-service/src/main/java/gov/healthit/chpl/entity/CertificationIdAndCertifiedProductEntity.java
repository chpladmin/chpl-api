package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

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

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }
}
