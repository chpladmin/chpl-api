package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The certification criteria to which a given clinical quality measure applies.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@AllArgsConstructor
public class CQMResultCertification implements Serializable {
    private static final long serialVersionUID = 2547864525772721622L;

    /**
     * CQM to criteria mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Criteria internal ID
     */
    @XmlElement(required = true)
    private Long certificationId;

    /**
     * Certification number (i.e. 170.314 (c)(1)) of the criteria
     */
    @XmlElement(required = false, nillable = true)
    private String certificationNumber;

    @XmlTransient
    private CertificationCriterion criterion;

    public CQMResultCertification() {
    }

    public CQMResultCertification(CQMResultCriteriaDTO dto) {
        this.id = dto.getId();
        this.certificationId = dto.getCriterionId();
        this.criterion = dto.getCriterion();
        if (dto.getCriterion() != null) {
            this.certificationNumber = dto.getCriterion().getNumber();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(final Long criteriaId) {
        this.certificationId = criteriaId;
    }

    public String getCertificationNumber() {
        return certificationNumber;
    }

    public void setCertificationNumber(final String criteriaNumber) {
        this.certificationNumber = criteriaNumber;
    }

    public CertificationCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(CertificationCriterion criterion) {
        this.criterion = criterion;
    }
}
