package gov.healthit.chpl.questionableactivity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
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
@Table(name = "questionable_activity_certification_result")
public class QuestionableActivityCertificationResultEntity extends EntityAudit implements QuestionableActivityBaseEntity {
    private static final long serialVersionUID = -879393016829926690L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "questionable_activity_trigger_id")
    private Long triggerId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", insertable = true, updatable = false)
    private ActivityEntity activity;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "questionable_activity_trigger_id", insertable = false, updatable = false)
    private QuestionableActivityTriggerEntity trigger;

    @Column(name = "certification_result_id")
    private Long certResultId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_result_id", insertable = false, updatable = false)
    private CertificationResultDetailsEntity certResult;

    @Column(name = "before_data")
    private String before;

    @Column(name = "after_data")
    private String after;

    @Column(name = "reason")
    private String reason;

    @Column(name = "activity_date")
    private Date activityDate;

    @Column(name = "activity_user_id")
    private Long userId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_user_id", insertable = false, updatable = false)
    private UserEntity user;

    public QuestionableActivityCertificationResult toDomain() {
        return QuestionableActivityCertificationResult.builder()
                .id(this.getId())
                .activity(this.getActivity().toDomain())
                .trigger(this.getTrigger() == null
                    ? QuestionableActivityTrigger.builder().id(this.getTriggerId()).build()
                            : this.getTrigger().toDomain())
                .before(this.getBefore())
                .after(this.getAfter())
                .activityDate(this.getActivityDate())
                .listing(this.getCertResult() == null ? null
                    : new CertifiedProductDetailsDTO(this.getCertResult().getListing()))
                .certResultId(this.getCertResultId())
                .certResult(this.getCertResult() == null
                    ? CertificationResultDetailsDTO.builder().id(this.getCertResultId()).build()
                            : new CertificationResultDetailsDTO(this.getCertResult()))
                .reason(this.getReason())
                .build();
    }
}
