package gov.healthit.chpl.email.footer;

import java.io.IOException;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;

public class SubscriptionFooter extends Footer {
    private String feedbackUrlTag;
    private String additionalContentTag;
    private String publicContactUrl;
    private String unsubscribeText;
    private SubscriptionLookupUtil lookupUtil;
    private Subscriber subscriber;

    public SubscriptionFooter(Resource htmlFooterResource,
            String feedbackUrlTag,
            String additionalContentTag,
            Environment env) throws IOException {
        super(htmlFooterResource);
        this.unsubscribeText = env.getProperty("subscriptions.unsubscribe");
        this.publicContactUrl = env.getProperty("contact.publicUrl");
        this.feedbackUrlTag = feedbackUrlTag;
        this.additionalContentTag = additionalContentTag;
    }

    public SubscriptionFooter lookupUtil(SubscriptionLookupUtil subscriptionLookupUtil) {
        this.lookupUtil = subscriptionLookupUtil;
        return this;
    }

    public SubscriptionFooter subscriber(Subscriber subscriberObj) {
        this.subscriber = subscriberObj;
        return this;
    }

    public String build() {
        String modifiedHtmlFooter = new String(getFooterHtml());
        modifiedHtmlFooter = modifiedHtmlFooter.replace(feedbackUrlTag, publicContactUrl);
        modifiedHtmlFooter = modifiedHtmlFooter.replace(additionalContentTag,
                " " + String.format(unsubscribeText, lookupUtil.getUnsubscribeUrl(subscriber)));
        return modifiedHtmlFooter;
    }
}