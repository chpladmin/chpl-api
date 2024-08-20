package gov.healthit.chpl.scheduler.job.subscriptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter.ObservationSubjectFormatterFactory;
import gov.healthit.chpl.scheduler.job.subscriptions.types.formatter.ObservationTypeFormatter;
import gov.healthit.chpl.scheduler.job.subscriptions.types.formatter.ObservationTypeFormatterFactory;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class ObservationProcessor {
    private SubscriptionObservationDao observationDao;
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder htmlEmailBuilder;

    private String notificationEmailSubject;
    private String notificationEmailIntroduction;
    private String manageSubscriptionsParagraph;
    private Boolean sendNotificationEmails;
    private ObservationTypeFormatterFactory observationTypeFormatterFactory;
    private ObservationSubjectFormatterFactory observationSubjectFormatterFactory;
    private SubscriptionLookupUtil lookupUtil;
    private List<String> observationTableHeadings;

    @Autowired
    public ObservationProcessor(SubscriptionObservationDao observationDao,
            ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder htmlEmailBuilder,
            @Value("${observation.notification.subject}") String notificationEmailSubject,
            @Value("${observation.notification.introduction}") String notificationEmailIntroduction,
            @Value("${subscriptions.manage}") String manageSubscriptionsParagraph,
            @Value("${observation.notification.sendEmail}") Boolean sendNotificationEmails,
            ObservationTypeFormatterFactory observationTypeFormatterFactory,
            ObservationSubjectFormatterFactory observationSubjectFormatterFactory,
            SubscriptionLookupUtil lookupUtil) {
        this.observationDao = observationDao;
        this.chplEmailFactory = chplEmailFactory;
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.notificationEmailSubject = notificationEmailSubject;
        this.notificationEmailIntroduction = notificationEmailIntroduction;
        this.sendNotificationEmails = sendNotificationEmails;
        this.manageSubscriptionsParagraph = manageSubscriptionsParagraph;
        this.observationTypeFormatterFactory = observationTypeFormatterFactory;
        this.observationSubjectFormatterFactory = observationSubjectFormatterFactory;
        this.lookupUtil = lookupUtil;
        this.observationTableHeadings = Stream.of("Change Type", "Change Details", "Change Date").toList();
    }

    @Transactional
    public void processObservations(Subscriber subscriber, List<SubscriptionObservation> observations) {
        LOGGER.info("Processing obsevations for " + subscriber.getEmail());
        try {
            if (sendNotificationEmails) {
                chplEmailFactory.emailBuilder()
                    .recipients(List.of(subscriber.getEmail()))
                    .subject(notificationEmailSubject)
                    .htmlMessage(buildMessage(observations))
                    .sendEmail();
            }
            markObservationsAsNotified(observations);
        } catch (EmailNotSentException msgEx) {
            LOGGER.error("Could not send email about failed listing upload: " + msgEx.getMessage(), msgEx);
        }
    }

    private String buildMessage(List<SubscriptionObservation> observations) {
        Subscriber subscriber = observations.get(0).getSubscriber();
        return htmlEmailBuilder.initialize()
            .heading(notificationEmailSubject)
            .paragraph(null, notificationEmailIntroduction)
            .customHtml(getObservationsHtml(observations))
            .paragraph(null, String.format(manageSubscriptionsParagraph, lookupUtil.getManageUrl(subscriber)))
            .footerSubscription(subscriber)
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
        ObservationTypeFormatter formatter = observationTypeFormatterFactory.getObjectTypeFormatter(observationsForSubscribedObject.get(0));
        String subscribedItemHeading = formatter.getSubscribedItemHeading(observationsForSubscribedObject.get(0));
        String subscribedItemFooter = formatter.getSubscribedItemFooter(observationsForSubscribedObject.get(0));

        String observationsTable = htmlEmailBuilder.getTableHtml(observationTableHeadings,
                toTableRowsLists(observationsForSubscribedObject),
                "No observations were found.",
                subscribedItemFooter);

        return htmlEmailBuilder.getParagraphHtml(subscribedItemHeading, observationsTable, "h3");
    }

    private List<List<String>> toTableRowsLists(List<SubscriptionObservation> observationsForSubscribedObject) {
        List<List<String>> observationsTabularLists = new ArrayList<List<String>>();
        observationsForSubscribedObject.stream()
            .map(observation -> toTableRowLists(observation))
            .filter(rowsForObservationTable -> rowsForObservationTable != null)
            .flatMap(rowsForObservationTable -> rowsForObservationTable.stream())
            .forEach(rowForObservationTable -> observationsTabularLists.add(rowForObservationTable));
        return observationsTabularLists;
    }

    private List<List<String>> toTableRowLists(SubscriptionObservation observation) {
        return observationSubjectFormatterFactory.getSubjectFormatter(observation).toListsOfStrings(observation);
    }

    private void markObservationsAsNotified(List<SubscriptionObservation> observations) {
        observationDao.markObservationsAsNotified(observations.stream().map(obs -> obs.getId()).toList());
    }
}
