package gov.healthit.chpl.complaint.entity;

import org.hibernate.annotations.SQLRestriction;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.complaint.domain.ComplaintCriterionMap;
import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "complaint_criterion_map")
public class ComplaintCriterionMapEntity extends EntityAudit {
    private static final long serialVersionUID = -7852427620682321127L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_criterion_map_id", nullable = false)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    @SQLRestriction("deleted <> true")
    private CertificationCriterionEntity certificationCriterion;

    public ComplaintCriterionMap buildComplaintCriterionMap() {
        return ComplaintCriterionMap.builder()
        .certificationCriterionId(this.getCertificationCriterionId())
        .certificationCriterion(this.getCertificationCriterion() != null ? this.getCertificationCriterion().toDomain() : null)
        .complaintId(this.getComplaintId())
        .id(this.getId())
        .build();
    }
}
