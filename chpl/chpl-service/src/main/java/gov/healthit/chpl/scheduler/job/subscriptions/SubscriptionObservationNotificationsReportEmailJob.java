package gov.healthit.chpl.scheduler.job.subscriptions;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "subscriptionObservationNotificationsReportEmailJobLogger")
public class SubscriptionObservationNotificationsReportEmailJob  implements Job {
    public static final String JOB_NAME = "subscriptionObservationNotificationsReportEmailJob";
    public static final String EMAIL_PARAM = "email";

    @Autowired
    private SubscriptionObservationDao observationDao;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Subscription Observation Notifications Report Email Job *********");

        try {
            //get all observations with a notified-at timestamp
            List<SubscriptionObservation> observationsWithNotificationTime = getObservations();
            LOGGER.info("Found " + observationsWithNotificationTime.size() + " observation notifications.");

            //TODO: send email with observations written as csv attachment

        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Subscription Observation Notifications Report Email Job *********");
        }
    }

    private List<SubscriptionObservation> getObservations() {
        return observationDao.getObservationsNotified();
    }
}
