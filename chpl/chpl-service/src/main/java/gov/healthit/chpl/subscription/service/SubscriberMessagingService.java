package gov.healthit.chpl.subscription.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.subscription.domain.Subscriber;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SubscriberMessagingService {
    private SubscriptionLookupUtil lookupUtil;
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder emailBuilder;

    private String pendingSubscriberSubject;
    private String pendingSubscriberBody1;
    private String welcomeSubscriberSubject;
    private String welcomeSubscriberBody1;
    private String welcomeSubscriberBody2;

    @Autowired
    public SubscriberMessagingService(ChplEmailFactory chplEmailFactory,
            ChplHtmlEmailBuilder emailBuilder,
            @Value("${subscriber.confirmMessage.subject}") String pendingSubscriberSubject,
            @Value("${subscriber.confirmMessage.paragraph1}") String pendingSubscriberBody1,
            @Value("${subscriber.welcomeMessage.subject}") String welcomeSubscriberSubject,
            @Value("${subscriber.welcomeMessage.paragraph1}") String welcomeSubscriberBody1,
            @Value("${subscriber.welcomeMessage.paragraph2}") String welcomeSubscriberBody2,
            SubscriptionLookupUtil lookupUtil) {
        this.chplEmailFactory = chplEmailFactory;
        this.emailBuilder = emailBuilder;
        this.lookupUtil = lookupUtil;
        this.pendingSubscriberSubject = pendingSubscriberSubject;
        this.pendingSubscriberBody1 = pendingSubscriberBody1;
        this.welcomeSubscriberSubject = welcomeSubscriberSubject;
        this.welcomeSubscriberBody1 = welcomeSubscriberBody1;
        this.welcomeSubscriberBody2 = welcomeSubscriberBody2;
    }

    public void sendConfirmation(Subscriber subscriber) {
        String htmlMessage = emailBuilder.initialize()
                .heading(pendingSubscriberSubject)
                .paragraph(null, String.format(pendingSubscriberBody1, lookupUtil.getConfirmationUrl(subscriber)))
                .footer(PublicFooter.class)
                .build();

        try {
            chplEmailFactory.emailBuilder()
                    .recipients(List.of(subscriber.getEmail()))
                    .subject(pendingSubscriberSubject)
                    .htmlMessage(htmlMessage)
                    .sendEmail();
        } catch (EmailNotSentException msgEx) {
            LOGGER.error("Could not send confirmation email to subscriber " + subscriber.getEmail(), msgEx);
        }
    }

    public void sendWelcome(Subscriber subscriber) {
        String htmlMessage = emailBuilder.initialize()
                .heading(welcomeSubscriberSubject)
                .paragraph(null, welcomeSubscriberBody1)
                .paragraph(null, String.format(welcomeSubscriberBody2, lookupUtil.getManageUrl(subscriber)))
                .footer(PublicFooter.class)
                .build();

        try {
            chplEmailFactory.emailBuilder()
                    .recipients(List.of(subscriber.getEmail()))
                    .subject(welcomeSubscriberSubject)
                    .htmlMessage(htmlMessage)
                    .sendEmail();
        } catch (EmailNotSentException msgEx) {
            LOGGER.error("Could not send welcome email to subscriber " + subscriber.getEmail(), msgEx);
        }
    }

    public void sendManagementLink(Subscriber subscriber) {
        //TODO in a future ticket, send the subscriber a link with their UUID so they can manage their subscriptions
    }
}
