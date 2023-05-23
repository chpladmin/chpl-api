package gov.healthit.chpl.scheduler.job.subscriptions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

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
    private ObservationProcessor observationProcessor;

    private SubscriptionConsolidationMethod consolidationMethod;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the " + context.getMergedJobDataMap().getString(CONSOLIDATION_METHOD_PARAM)
                + " Subscription Observations Notification Email Job *********");
        getConsolidationMethodFromJobContext(context);
        if (this.consolidationMethod == null) {
            LOGGER.error("Unable to process observations consolidated '" + context.getMergedJobDataMap().getString(CONSOLIDATION_METHOD_PARAM) + "'");
            return;
        }

        try {
            Map<Subscriber, List<SubscriptionObservation>> observationsGroupedBySubscriber
                = getObservations().stream().collect(Collectors.groupingBy(SubscriptionObservation::getSubscriber));

            observationsGroupedBySubscriber.keySet().stream().forEach(
                    subscriber -> observationProcessor.processObservations(subscriber, observationsGroupedBySubscriber.get(subscriber)));
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the " + context.getMergedJobDataMap().getString(CONSOLIDATION_METHOD_PARAM)
                    + " Subscription Observations Notification Email Job *********");
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
}
