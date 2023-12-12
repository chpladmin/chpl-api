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

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityDeveloper;
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
@Table(name = "questionable_activity_developer")
public class QuestionableActivityDeveloperEntity extends EntityAudit implements QuestionableActivityBaseEntity {
    private static final long serialVersionUID = 4160513035384106983L;

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

    @Column(name = "developer_id")
    private Long developerId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", insertable = false, updatable = false)
    private DeveloperEntity developer;

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

    public QuestionableActivityDeveloper toDomain() {
        return QuestionableActivityDeveloper.builder()
                .id(this.getId())
                .activityId(this.getActivityId())
                .trigger(this.getTrigger() == null
                    ? QuestionableActivityTrigger.builder().id(this.getTriggerId()).build()
                            : this.getTrigger().toDomain())
                .before(this.getBefore())
                .after(this.getAfter())
                .activityDate(this.getActivityDate())
                .userId(this.getUserId())
                .developerId(this.getDeveloperId())
                .developer(this.getDeveloper() == null
                    ? Developer.builder().id(this.getDeveloperId()).build()
                            : this.getDeveloper().toDomain())
                .reason(this.getReason())
                .build();
    }
}

