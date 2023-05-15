package gov.healthit.chpl.questionableactivity.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.questionableactivity.domain.QuestionableActivity;
import gov.healthit.chpl.util.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Immutable
@Getter
@Setter
@ToString
@Table(name = "questionable_activity_combined")
public class QuestionableActivityEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "trigger_level")
    private String triggerLevel;

    @Column(name = "trigger_name")
    private String triggerName;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "before_data")
    private String before;

    @Column(name = "after_data")
    private String after;

    @Column(name = "activity_date")
    private Date activityDate;

    @Column(name = "activity_user_id")
    private Long userId;

    @Column(name = "user_contact_info")
    private String username;

    @Column(name = "certification_status_change_reason")
    private String certificationStatusChangeReason;

    @Column(name = "reason")
    private String reason;

    @Column(name = "developer_id")
    private Long developerId;

    @Column(name = "developer_name")
    private String developerName;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "version_id")
    private Long versionId;

    @Column(name = "version_name")
    private String versionName;

    @Column(name = "certified_product_id")
    private Long listingId;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_body_id")
    private Long acbId;

    @Column(name = "certification_body_name")
    private String acbName;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @Column(name = "certification_status_name")
    private String certificationStatusName;

    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;

    public QuestionableActivity toDomain() {
        return QuestionableActivity.builder()
                .id(id)
                .triggerLevel(triggerLevel)
                .triggerName(triggerName)
                .activityId(activityId)
                .before(before)
                .after(after)
                .activityDate(DateUtil.toLocalDateTime(activityDate.getTime()))
                .userId(userId)
                .username(username)
                .certificationStatusChangeReason(certificationStatusChangeReason)
                .reason(reason)
                .developerId(developerId)
                .developerName(developerName)
                .productId(productId)
                .productName(productName)
                .versionId(versionId)
                .versionName(versionName)
                .listingId(listingId)
                .chplProductNumber(chplProductNumber)
                .acbId(acbId)
                .acbName(acbName)
                .certificationStatusId(certificationStatusId)
                .certificationStatusName(certificationStatusName)
                .certificationCriterionId(certificationCriterionId)
                .build();
    }
}

