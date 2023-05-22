package gov.healthit.chpl.scheduler.job.subscriptions;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriptionConsolidationMethod;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class SubscriptionObservationsNotificationJob  implements Job {
    private static final String CONSOLIDATION_METHOD_PARAM = "consolidationMethod";

    @Autowired
    private SubscriptionDao subscriptionDao;

    @Autowired
    private SubscriptionObservationDao observationDao;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    private SubscriptionConsolidationMethod consolidationMethod;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the " + context.getMergedJobDataMap().getString(CONSOLIDATION_METHOD_PARAM)
                + " Subscription Observation Email Job *********");
        getConsolidationMethodFromJobContext(context);
        if (this.consolidationMethod == null) {
            LOGGER.error("Unable to process observations consolidated '" + context.getMergedJobDataMap().getString(CONSOLIDATION_METHOD_PARAM) + "'");
            return;
        }

        try {
            Map<Subscriber, List<SubscriptionObservation>> observationsGroupedBySubscriber
                = getObservations().stream()
                    .collect(Collectors.groupingBy(SubscriptionObservation::getSubscriber));

            //notify the subscriber about their relevant observations + delete those observations in one tx
//            sendEmails(context, observationsToNotify);
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the " + context.getMergedJobDataMap().getString(CONSOLIDATION_METHOD_PARAM)
                    + " Subscription Observation Email Job *********");
        }
    }

    private void getConsolidationMethodFromJobContext(JobExecutionContext context) {
        List<SubscriptionConsolidationMethod> allConsolidationMethods = subscriptionDao.getAllConsolidationMethods();
        String consolidationMethodFromContext = context.getMergedJobDataMap().getString(CONSOLIDATION_METHOD_PARAM);
        Optional<SubscriptionConsolidationMethod> cmOpt = allConsolidationMethods.stream()
            .filter(cm -> cm.getName().equalsIgnoreCase(consolidationMethodFromContext))
            .findAny();
        if (cmOpt.isPresent()) {
            this.consolidationMethod = cmOpt.get();
        }
    }

    private List<SubscriptionObservation> getObservations() {
        return observationDao.getObservations(this.consolidationMethod.getId());
    }

    private void sendEmails(List<SubscriptionObservation> observations) throws EmailNotSentException, IOException {
        //TODO: some batching or metering of these emails because we know there is a limit to how
        //many can be sent at once with Graph

//        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
//        chplEmailFactory.emailBuilder()
//                .recipient(context.getMergedJobDataMap().getString("email"))
//                .subject(env.getProperty("listingValidationReport.subject"))
//                .htmlMessage(createHtmlMessage(context, rows.size()))
//                .fileAttachments(Arrays.asList(listingValidationReportCsvCreator.createCsvFile(rows)))
//                .sendEmail();
//        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }
//
//    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
//        return chplHtmlEmailBuilder.initialize()
//                .heading(env.getProperty("listingValidationReport.subject"))
//                .paragraph(
//                        env.getProperty("listingValidationReport.paragraph1.heading"),
//                        getAcbNamesAsBrSeparatedList(context))
//                .paragraph("", String.format(env.getProperty("listingValidationReport.paragraph2.body"), errorCount))
//                .footer(true)
//                .build();
//    }
}
