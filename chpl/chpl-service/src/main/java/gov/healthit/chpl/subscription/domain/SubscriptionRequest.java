package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequest {
    private String email;
    private String reason;
    private Long subscriptionTypeId; // Listing, Developer, Product
    private Long subscribedObjectId; // listing id, developer id, or product id
}
