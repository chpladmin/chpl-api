package gov.healthit.chpl.subscription.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionLookupUtil {
    private Environment environment;

    @Autowired
    public SubscriptionLookupUtil(Environment environment) {
        this.environment = environment;
    }

    public Long getListingObjectTypeId() {
        return Long.parseLong(environment.getProperty("subscription.objectType.listing"));
    }

    public Long getDeveloperObjectTypeId() {
        return Long.parseLong(environment.getProperty("subscription.objectType.developer"));
    }

    public Long getProductObjectTypeId() {
        return Long.parseLong(environment.getProperty("subscription.objectType.product"));
    }

    public Long getDailyConsolidationMethodId() {
        return Long.parseLong(environment.getProperty("subscription.consolidationMethod.daily"));
    }

    public Long getWeeklyConsolidationMethodId() {
        return Long.parseLong(environment.getProperty("subscription.consolidationMethod.weekly"));
    }

    public Long getPendingSubscriberStatusId() {
        return Long.parseLong(environment.getProperty("subscription.subscriberStatus.pending"));
    }

    public Long getConfirmedSubscriberStatusId() {
        return Long.parseLong(environment.getProperty("subscription.subscriberStatus.confirmed"));
    }
}
