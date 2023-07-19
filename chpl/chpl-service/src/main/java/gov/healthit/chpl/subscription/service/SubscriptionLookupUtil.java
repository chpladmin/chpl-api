package gov.healthit.chpl.subscription.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.domain.Subscriber;

@Component
public class SubscriptionLookupUtil {
    private Environment environment;
    private String chplUrlBegin;

    @Autowired
    public SubscriptionLookupUtil(Environment environment) {
        this.environment = environment;
        this.chplUrlBegin = environment.getProperty("chplUrlBegin");
    }

    public String getConfirmationUrl(Subscriber subscriber) {
        String unformattedConfirmationUrl = chplUrlBegin + environment.getProperty("subscriber.confirm.url");
        return String.format(unformattedConfirmationUrl, subscriber.getId().toString());
    }

    public String getManageUrl(Subscriber subscriber) {
        String unformattedManageeUrl = chplUrlBegin + environment.getProperty("subscriptions.manage.url");
        return String.format(unformattedManageeUrl, subscriber.getId().toString());
    }

    public String getUnsubscribeUrl(Subscriber subscriber) {
        String unformattedUnsubscribeUrl = chplUrlBegin + environment.getProperty("subscriptions.unsubscribe.url");
        return String.format(unformattedUnsubscribeUrl, subscriber.getId().toString());
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

    public Long getCertificationStatusChangedSubjectId() {
        return Long.parseLong(environment.getProperty("subscription.subject.certificationStatusChanged"));
    }

    public Long getCertificationCriteriaAddedSubjectId() {
        return Long.parseLong(environment.getProperty("subscription.subject.certificationCriteriaAdded"));
    }

    public Long getCertificationCriteriaRemovedSubjectId() {
        return Long.parseLong(environment.getProperty("subscription.subject.certificationCriteriaRemoved"));
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
