package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.domain.complaint.ComplaintCriterionMap;
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

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Transient
    private CertificationCriterionEntity certificationCriterion;

    public ComplaintCriterionMap buildComplaintCriterionMap() {
        return ComplaintCriterionMap.builder()
        .certificationCriterionId(this.getCertificationCriterionId())
        .certificationCriterion(this.getCertificationCriterion() != null ? this.getCertificationCriterion().buildCertificationCriterion() : null)
        .complaintId(this.getComplaintId())
        .id(this.getId())
        .build();
    }
}
