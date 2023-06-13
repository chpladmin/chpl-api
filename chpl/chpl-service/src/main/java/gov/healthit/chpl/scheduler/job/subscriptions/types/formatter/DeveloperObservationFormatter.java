package gov.healthit.chpl.scheduler.job.subscriptions.types.formatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class DeveloperObservationFormatter extends ObservationTypeFormatter {

    private DeveloperDAO developerDao;

    @Autowired
    public DeveloperObservationFormatter(DeveloperDAO developerDao, Environment env) {
        super(env);
        this.developerDao = developerDao;
    }

    public String getSubscribedItemHeading(SubscriptionObservation observation) {
        Developer developer = getDeveloper(observation.getSubscription().getSubscribedObjectId());
        if (developer == null) {
            LOGGER.error("Cannot process subscription observation " + observation.getId());
            return "";
        }
        return developer.getName();
    }

    public String getSubscribedItemFooter(SubscriptionObservation observation) {
        Developer developer = getDeveloper(observation.getSubscription().getSubscribedObjectId());
        if (developer == null) {
            LOGGER.error("Cannot process subscription observation " + observation.getId());
            return "";
        }
        String developerUrl = String.format(getUnformattedDeveloperDetailsUrl(), developer.getId());
        return String.format(getUnformattedSubscribedItemFooter(), "developer", developerUrl, developer.getName());
    }

    private Developer getDeveloper(Long developerId) {
        Developer developer = null;
        try {
            developer = developerDao.getSimpleDeveloperById(developerId, false);
        } catch (Exception ex) {
            LOGGER.error("Developer with ID " + developerId + " not found.", ex);
            return null;
        }
        return developer;
    }
}
