package gov.healthit.chpl.subscription.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.subscription.search.SubscriptionSearchResult;
import gov.healthit.chpl.util.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "subscription_search_result")
public class SubscriptionSearchResultEntity {
    public static final String SUBJECT_SPLIT_CHAR = ",";

    @EmbeddedId
    private SubscriptionSearchResultId id;

    @Column(name = "subscriber_id", insertable = false, updatable = false)
    private UUID subscriberId;

    @Column(name = "subscriber_email", insertable = false, updatable = false)
    private String subscriberEmail;

    @Column(name = "subscriber_role", insertable = false, updatable = false)
    private String subscriberRole;

    @Column(name = "subscriber_status", insertable = false, updatable = false)
    private String subscriberStatus;

    @Column(name = "subscription_subjects", insertable = false, updatable = false)
    private String subscriptionSubjects;

    @Column(name = "subscription_object_type", insertable = false, updatable = false)
    private String subscriptionObjectType;

    @Column(name = "subscription_consolidation_method", insertable = false, updatable = false)
    private String subscriptionConsolidationMethod;

    @Column(name = "subscribed_object_id", insertable = false, updatable = false)
    private Long subscribedObjectId;

    @Column(name = "subscribed_object_name", insertable = false, updatable = false)
    private String subscribedObjectName;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    public SubscriptionSearchResult toDomain() {
        return SubscriptionSearchResult.builder()
                .creationDate(DateUtil.toLocalDateTime(getCreationDate().getTime()))
                .subscribedObjectId(getSubscribedObjectId())
                .subscribedObjectName(getSubscribedObjectName())
                .subscriberEmail(getSubscriberEmail())
                .subscriberId(getSubscriberId())
                .subscriberRole(getSubscriberRole())
                .subscriberStatus(getSubscriberStatus())
                .subscriptionConsolidationMethod(getSubscriptionConsolidationMethod())
                .subscriptionObjectType(getSubscriptionObjectType())
                .subscriptionSubjects(subjectsStringToSet(getSubscriptionSubjects()))
                .build();
    }

    private Set<String> subjectsStringToSet(String subjects) {
        if (StringUtils.isEmpty(subjects)) {
            return new HashSet<String>();
        }
        String[] splitSubjects = subjects.split(SUBJECT_SPLIT_CHAR);
        if (splitSubjects == null || splitSubjects.length == 0) {
            return new HashSet<String>();
        }
        return Stream.of(splitSubjects).collect(Collectors.toSet());
    }
}

@Getter
@Setter
@NoArgsConstructor
@Embeddable
class SubscriptionSearchResultId implements Serializable {
    private static final long serialVersionUID = 3676396085983948227L;

    @Column(name = "subscriber_id")
    private UUID subscriberId;

    @Column(name = "subscription_object_type")
    private String subscriptionObjectType;

    @Column(name = "subscribed_object_id")
    private Long subscribedObjectId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subscriberId == null) ? 0 : subscriberId.hashCode());
        result = prime * result + ((subscribedObjectId == null) ? 0 : subscribedObjectId.hashCode());
        result = prime * result + ((subscriptionObjectType == null) ? 0 : subscriptionObjectType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SubscriptionSearchResultId other = (SubscriptionSearchResultId) obj;
        if (subscriberId == null) {
            if (other.subscriberId != null) {
                return false;
            }
        } else if (!subscriberId.equals(other.subscriberId)) {
            return false;
        }
        if (subscribedObjectId == null) {
            if (other.subscribedObjectId != null) {
                return false;
            }
        } else if (!subscribedObjectId.equals(other.subscribedObjectId)) {
            return false;
        }
        if (subscriptionObjectType == null) {
            if (other.subscriptionObjectType != null) {
                return false;
            }
        } else if (!subscriptionObjectType.equals(other.subscriptionObjectType)) {
            return false;
        }
        return true;
    }
}
