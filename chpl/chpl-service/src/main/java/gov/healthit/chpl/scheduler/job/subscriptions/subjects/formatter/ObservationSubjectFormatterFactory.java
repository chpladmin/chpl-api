package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class ObservationSubjectFormatterFactory {

    private CertificationStatusChangedFormatter certStatusChangedFormatter;
    private CertificationCriteriaAddedFormatter criteriaAddedFormatter;
    private SubscriptionLookupUtil lookupUtil;

    @Autowired
    public ObservationSubjectFormatterFactory(CertificationStatusChangedFormatter certStatusChangedFormatter,
            CertificationCriteriaAddedFormatter criteriaAddedFormatter,
            SubscriptionLookupUtil lookupUtil) {
        this.certStatusChangedFormatter = certStatusChangedFormatter;
        this.criteriaAddedFormatter = criteriaAddedFormatter;
        this.lookupUtil = lookupUtil;
    }

    public ObservationSubjectFormatter getSubjectFormatter(SubscriptionObservation observation) {
        Long observationSubjectId = observation.getSubscription().getSubject().getId();
        if (lookupUtil.getCertificationStatusChangedSubjectId().equals(observationSubjectId)) {
            return certStatusChangedFormatter;
        } else if (lookupUtil.getCertificationCriteriaAddedSubjectId().equals(observationSubjectId)) {
            return criteriaAddedFormatter;
        } else {
            LOGGER.error("No subject ormatter found for subject with ID " + observationSubjectId);
        }
        return null;
    }
}
