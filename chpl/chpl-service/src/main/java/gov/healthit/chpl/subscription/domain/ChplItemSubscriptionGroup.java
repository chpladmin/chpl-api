package gov.healthit.chpl.subscription.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ChplItemSubscriptionGroup {
    private List<ChplItemSubscription> subscriptions;
}
