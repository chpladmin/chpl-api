package gov.healthit.chpl.complaint.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.complaint.domain.ComplaintCriterionMap;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "complaint_criterion_map")
public class ComplaintCriterionMapEntity {
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
    @Where(clause = "deleted <> 'true'")
    private CertificationCriterionEntity certificationCriterion;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public ComplaintCriterionMap buildComplaintCriterionMap() {
        return ComplaintCriterionMap.builder()
        .certificationCriterionId(this.getCertificationCriterionId())
        .certificationCriterion(this.getCertificationCriterion() != null ? this.getCertificationCriterion().toDomain() : null)
        .complaintId(this.getComplaintId())
        .id(this.getId())
        .build();
    }
}
