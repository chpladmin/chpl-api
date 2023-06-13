package gov.healthit.chpl.scheduler.job.subscriptions.types.formatter;

import org.springframework.core.env.Environment;

import gov.healthit.chpl.subscription.domain.SubscriptionObservation;

public abstract class ObservationTypeFormatter {
    private String subscribedItemFooter;
    private String listingDetailsUrl;
    private String developerDetailsUrl;

    public ObservationTypeFormatter(Environment env) {
        this.subscribedItemFooter = env.getProperty("observation.notification.subscribedItemFooter");
        this.listingDetailsUrl = env.getProperty("chplUrlBegin") + env.getProperty("listingDetailsUrlPart");
        this.developerDetailsUrl = env.getProperty("chplUrlBegin") + env.getProperty("developerDetailsUrlPart");
    }

    public String getUnformattedSubscribedItemFooter() {
        return subscribedItemFooter;
    }

    public String getUnformattedListingDetailsUrl() {
        return listingDetailsUrl;
    }

    public String getUnformattedDeveloperDetailsUrl() {
        return developerDetailsUrl;
    }

    public abstract String getSubscribedItemHeading(SubscriptionObservation observation);
    public abstract String getSubscribedItemFooter(SubscriptionObservation observation);
}
