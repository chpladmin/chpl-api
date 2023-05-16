package gov.healthit.chpl.questionableactivity.entity;

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

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "questionable_activity_certification_result")
public class QuestionableActivityCertificationResultEntity implements QuestionableActivityBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "questionable_activity_trigger_id")
    private Long triggerId;

    @Column(name = "activity_id")
    private Long activityId;

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

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public QuestionableActivityCertificationResult toDomain() {
        return QuestionableActivityCertificationResult.builder()
                .id(this.getId())
                .activityId(this.getActivityId())
                .trigger(this.getTrigger() == null
                    ? QuestionableActivityTrigger.builder().id(this.getTriggerId()).build()
                            : this.getTrigger().toDomain())
                .before(this.getBefore())
                .after(this.getAfter())
                .activityDate(this.getActivityDate())
                .userId(this.getUserId())
                .listing(new CertifiedProductDetailsDTO(this.getCertResult().getListing()))
                .certResultId(this.getCertResultId())
                .certResult(this.getCertResult() == null
                    ? CertificationResultDetailsDTO.builder().id(this.getCertResultId()).build()
                            : new CertificationResultDetailsDTO(this.getCertResult()))
                .reason(this.getReason())
                .build();
    }
}

