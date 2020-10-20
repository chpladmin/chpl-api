package gov.healthit.chpl.listing.mipsMeasure;

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

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "allowed_mips_measure_criteria")
public class MipsMeasureCriterionMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "mips_measure_id", nullable = false)
    private Long mipsMeasureId;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", unique = true, nullable = true, insertable = false, updatable = false)
    private CertificationCriterionEntity criterion;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public CertificationCriterion convert() {
        if (getCriterion() == null) {
            return null;
        }
        CertificationCriterionEntity ccEntity = getCriterion();
        CertificationCriterion cert = new CertificationCriterion();
        cert.setId(getCertificationCriterionId());
        cert.setNumber(ccEntity.getNumber());
        cert.setRemoved(ccEntity.getRemoved());
        cert.setTitle(ccEntity.getTitle());
        cert.setCertificationEditionId(ccEntity.getCertificationEditionId());
        if (ccEntity.getCertificationEdition() != null) {
            cert.setCertificationEdition(ccEntity.getCertificationEdition().getYear());
        }
        return cert;
    }
}
