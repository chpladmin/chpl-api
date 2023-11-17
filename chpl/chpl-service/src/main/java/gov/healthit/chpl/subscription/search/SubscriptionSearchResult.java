package gov.healthit.chpl.subscription.search;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;

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

    private Long id;
    private UUID subscriberId;
    private String subscriberEmail;
    private String subscriberStatus;
    private String subscriberRole;
    private String subscriptionObjectType; // "Listing", "Developer", "Product"
    private String subscriptionSubject; // "Certification Status Changed", "Attested Criteria"
    private Long subscribedObjectId; // listing ID, developer ID, etc
    private String subscribedObjectName; // "15.04.04.CODE...", "Epic Systems Inc".

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creationDate;

    public SubscriptionSearchResult() {
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (!(another instanceof SubscriptionSearchResult)) {
            return false;
        }
        SubscriptionSearchResult anotherSearchResult = (SubscriptionSearchResult) another;
        if (ObjectUtils.allNotNull(this, anotherSearchResult, this.getId(), anotherSearchResult.getId())) {
            return Objects.equals(this.getId(), anotherSearchResult.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }
}
