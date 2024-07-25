package gov.healthit.chpl.scheduler.job.subscriptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "subscriptionObservationNotificationsReportEmailJobLogger")
public class SubscriptionObservationNotificationsReportEmailJob  implements Job {
    public static final String JOB_NAME = "Subscription Notifications Email Report";
    public static final String EMAIL_KEY = "email";

    @Autowired
    private SubscriptionObservationDao observationDao;

    @Autowired
    private SubscriptionObservationNotificationsReportCsvCreator csvCreator;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Subscription Observation Notifications Report Email Job *********");

        try {
            //get all observations with a notified-at timestamp
            List<SubscriptionObservation> observationsWithNotificationTime = getObservations();
            LOGGER.info("Found " + observationsWithNotificationTime.size() + " observation notifications.");

            //TODO: Fill in subscribed object name

            //send email with observations written as csv attachment
            sendEmail(context, observationsWithNotificationTime);
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Subscription Observation Notifications Report Email Job *********");
        }
    }

    private List<SubscriptionObservation> getObservations() {
        return observationDao.getObservationsNotified();
    }

    private void sendEmail(JobExecutionContext context, List<SubscriptionObservation> rows) throws EmailNotSentException, IOException {
        String email = context.getMergedJobDataMap().getString(EMAIL_KEY);
        LOGGER.info("Sending email to: " + email);
        chplEmailFactory.emailBuilder()
                .recipient(email)
                .subject(env.getProperty("subscriptionObservationNotificationsReport.subject"))
                .htmlMessage(createHtmlMessage(context, rows.size()))
                .fileAttachments(Arrays.asList(csvCreator.createCsvFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + email);
    }

    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("subscriptionObservationNotificationsReport.heading"))
                .paragraph(
                        "",
                        env.getProperty("subscriptionObservationNotificationsReport.paragraph1.body"))
                .footer(PublicFooter.class)
                .build();
    }
}
