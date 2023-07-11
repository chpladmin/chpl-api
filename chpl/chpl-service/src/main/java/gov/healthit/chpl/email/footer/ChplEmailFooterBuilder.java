package gov.healthit.chpl.email.footer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChplEmailFooterBuilder {
    private static final String FEEDBACK_URL_TAG = "${feedback-url}";
    private static final String ADDITIONAL_CONTENT_TAG = "${additional-footer-content}";

    private Resource htmlFooterResource;
    private Environment env;
    private SubscriptionLookupUtil subscriptionLookupUtil;

    @Autowired
    public ChplEmailFooterBuilder(@Value("classpath:email/chpl-email-footer.html") Resource htmlFooterResource,
        Environment env,
        SubscriptionLookupUtil subscriptionLookupUtil) {
        this.htmlFooterResource = htmlFooterResource;
        this.env = env;
        this.subscriptionLookupUtil = subscriptionLookupUtil;
    }

    public String buildFooter(Class<? extends Footer> footerClazz) {
        Footer footer = null;
        try {
            footer = footerClazz.getDeclaredConstructor(Resource.class, String.class, String.class, Environment.class)
                        .newInstance(htmlFooterResource, FEEDBACK_URL_TAG, ADDITIONAL_CONTENT_TAG, env);
        } catch (Exception ex) {
            LOGGER.error("Unable to instantiate footer class " + footerClazz, ex);
        }

        if (footer == null) {
            return "";
        }
        return footer.build();
    }

    public String buildSubscriptionFooter(Subscriber subscriber) {
        SubscriptionFooter footer = null;
        try {
            footer = new SubscriptionFooter(htmlFooterResource, FEEDBACK_URL_TAG, ADDITIONAL_CONTENT_TAG, env);
        } catch (Exception ex) {
            LOGGER.error("Unable to instantiate SubscriptionFooter ", ex);
        }

        if (footer == null) {
            return "";
        }

        return footer
                .lookupUtil(subscriptionLookupUtil)
                .subscriber(subscriber)
                .build();
    }
}
