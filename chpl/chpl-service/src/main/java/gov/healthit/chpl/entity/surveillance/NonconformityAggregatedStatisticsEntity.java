package gov.healthit.chpl.entity.surveillance;

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

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.CertificationCriterionEntity;

@Entity
@Immutable
@Table(name = "aggregated_nonconformity_statistics")
public class NonconformityAggregatedStatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "nonconformity_count", nullable = false)
    private Long nonconformityCount;

    @Basic(optional = false)
    @Column(name = "non_criterion_type", nullable = false)
    private String nonconformityType;

    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterionEntity;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getNonconformityCount() {
        return nonconformityCount;
    }

    public void setNonconformityCount(Long nonconformityCount) {
        this.nonconformityCount = nonconformityCount;
    }

    public String getNonconformityType() {
        return nonconformityType;
    }

    public void setNonconformityType(String nonconformityType) {
        this.nonconformityType = nonconformityType;
    }

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public CertificationCriterionEntity getCertificationCriterionEntity() {
        return certificationCriterionEntity;
    }

    public void setCertificationCriterionEntity(CertificationCriterionEntity certificationCriterionEntity) {
        this.certificationCriterionEntity = certificationCriterionEntity;
    }
}
