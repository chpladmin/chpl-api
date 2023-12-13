package gov.healthit.chpl.subscription.search;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateTimeDeserializer;
import gov.healthit.chpl.util.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SubscriptionSearchResult implements Serializable {

    private static final long serialVersionUID = -2546253641921038L;

    private UUID subscriberId;
    private String subscriberEmail;
    private String subscriberStatus;
    private String subscriberRole;
    private String subscriptionObjectType; // "Listing", "Developer", "Product"
    private String subscriptionConsolidationMethod; // "Daily", "Weekly", "Push"
    private Set<String> subscriptionSubjects; // "Certification Status Changed", "Attested Criteria"
    private Long subscribedObjectId; // listing ID, developer ID, etc
    private String subscribedObjectName; // "15.04.04.CODE...", "Epic Systems Inc".

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creationDate;

    public SubscriptionSearchResult() {
    }

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
        SubscriptionSearchResult other = (SubscriptionSearchResult) obj;
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
