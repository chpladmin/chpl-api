package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    private Long id;
    private Subscriber subscriber;
    private SubscriptionSubject subject;
    private Long subscribedObjectId;
    private SubscriptionConsolidationMethod consolidationMethod;

}
