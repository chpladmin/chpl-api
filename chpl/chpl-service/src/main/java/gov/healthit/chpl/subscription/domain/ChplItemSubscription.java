package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChplItemSubscription {
    private Long id;
    private SubscriptionSubject subject;
    private SubscriptionConsolidationMethod consolidationMethod;
}
