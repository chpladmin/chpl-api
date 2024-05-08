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
    private CertificationCriteriaRemovedFormatter criteriaRemovedFormatter;
    private RwtPlansUrlChangedFormatter rwtPlansChangedFormatter;
    private RwtResultsUrlChangedFormatter rwtResultsChangedFormatter;
    private ServiceBaseUrlListChangedFormatter serviceBaseUrlListChangedFormatter;
    private SubscriptionLookupUtil lookupUtil;

    @Autowired
    public ObservationSubjectFormatterFactory(CertificationStatusChangedFormatter certStatusChangedFormatter,
            CertificationCriteriaAddedFormatter criteriaAddedFormatter,
            CertificationCriteriaRemovedFormatter criteriaRemovedFormatter,
            RwtPlansUrlChangedFormatter rwtPlansChangedFormatter,
            RwtResultsUrlChangedFormatter rwtResultsChangedFormatter,
            ServiceBaseUrlListChangedFormatter serviceBaseUrlListChangedFormatter,
            SubscriptionLookupUtil lookupUtil) {
        this.certStatusChangedFormatter = certStatusChangedFormatter;
        this.criteriaAddedFormatter = criteriaAddedFormatter;
        this.criteriaRemovedFormatter = criteriaRemovedFormatter;
        this.rwtPlansChangedFormatter = rwtPlansChangedFormatter;
        this.rwtResultsChangedFormatter = rwtResultsChangedFormatter;
        this.serviceBaseUrlListChangedFormatter = serviceBaseUrlListChangedFormatter;
        this.lookupUtil = lookupUtil;
    }

    public ObservationSubjectFormatter getSubjectFormatter(SubscriptionObservation observation) {
        Long observationSubjectId = observation.getSubscription().getSubject().getId();
        if (lookupUtil.getCertificationStatusChangedSubjectId().equals(observationSubjectId)) {
            return certStatusChangedFormatter;
        }
        if (lookupUtil.getCertificationCriteriaAddedSubjectId().equals(observationSubjectId)) {
            return criteriaAddedFormatter;
        }
        if (lookupUtil.getCertificationCriteriaRemovedSubjectId().equals(observationSubjectId)) {
            return criteriaRemovedFormatter;
        }
        if (lookupUtil.getRwtPlansUrlChangedSubjectId().equals(observationSubjectId)) {
            return rwtPlansChangedFormatter;
        }
        if (lookupUtil.getRwtResultsUrlChangedSubjectId().equals(observationSubjectId)) {
            return rwtResultsChangedFormatter;
        }
        if (lookupUtil.getServiceBaseUrlListChangedSubjectId().equals(observationSubjectId)) {
            return serviceBaseUrlListChangedFormatter;
        }
        LOGGER.error("No subject formatter found for subject with ID " + observationSubjectId);
        return null;
    }
}
