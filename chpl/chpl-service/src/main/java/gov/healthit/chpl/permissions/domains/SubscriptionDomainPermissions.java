package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.subscription.SearchActionPermissions;


@Component
public class SubscriptionDomainPermissions extends DomainPermissions {
    public static final String SEARCH = "SEARCH";

    @Autowired
    public SubscriptionDomainPermissions(
            @Qualifier("subscriptionSearchActionPermissions") SearchActionPermissions searchActionPermissions) {
        getActionPermissions().put(SEARCH, searchActionPermissions);
    }
}
