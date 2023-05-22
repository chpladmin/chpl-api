package gov.healthit.chpl.scheduler.job.subscriptions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ObservationProcessor {
    private SubscriptionObservationDao observationDao;
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder htmlEmailBuilder;

    private String notificationEmailSubject;

    @Autowired
    public ObservationProcessor(SubscriptionObservationDao observationDao,
            ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder htmlEmailBuilder,
            @Value("${observation.notification.subject}") String notificationEmailSubject) {
        this.observationDao = observationDao;
        this.chplEmailFactory = chplEmailFactory;
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.notificationEmailSubject = notificationEmailSubject;
    }

    @Transactional
    public void processObservations(Subscriber subscriber, List<SubscriptionObservation> observations) {
        try {
            chplEmailFactory.emailBuilder()
                    .recipients(List.of(subscriber.getEmail()))
                    .subject(notificationEmailSubject)
                    .htmlMessage(buildMessage(observations))
                    .sendEmail();
            deleteNotifiedObservations(observations);
        } catch (EmailNotSentException msgEx) {
            LOGGER.error("Could not send email about failed listing upload: " + msgEx.getMessage(), msgEx);
        }
    }

    private String buildMessage(List<SubscriptionObservation> observations) {
        return htmlEmailBuilder.initialize()
            .heading(notificationEmailSubject)
            .customHtml(getObservationsHtml(observations))
            .footer(true)
            .build();
    }

    private String getObservationsHtml(List<SubscriptionObservation> observations) {
        Map<Pair<Long, SubscriptionObjectType>, List<SubscriptionObservation>> observationsPerSubscribedObject
            = observations.stream().collect(Collectors.groupingBy(obs -> new ImmutablePair<>(
                        obs.getSubscription().getSubscribedObjectId(), obs.getSubscription().getSubject().getType())));

        StringBuffer observationsHtmlBuf = new StringBuffer();
        observationsPerSubscribedObject.keySet().stream()
            .map(observedItemKey -> getObservationHtmlForSubscribedObject(observationsPerSubscribedObject.get(observedItemKey)))
            .forEach(observedItemHtml -> observationsHtmlBuf.append(observedItemHtml));
        return observationsHtmlBuf.toString();
    }

    private String getObservationHtmlForSubscribedObject(List<SubscriptionObservation> observationsForSubscribedObject) {
        //all observations in this method are for the same object - the same listing, same developer, same product, whatever
        //need to pass something into a class that can build the heading, paragraph, and maybe table? of HTML
        //for this set of subscriptions
        //and need this to be a factory or some easily extensible thing
        //TODO
        return "";
        //htmlEmailBuilder.getParagraphHtml(observationsPerSubscribedObject, notificationEmailSubject, null);

    }

    private void deleteNotifiedObservations(List<SubscriptionObservation> observations) {
        observationDao.deleteObservations(observations.stream().map(obs -> obs.getId()).toList());
    }
}
