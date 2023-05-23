package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.util.List;

import gov.healthit.chpl.subscription.domain.SubscriptionObservation;

public abstract class ObservationSubjectFormatter {

    public abstract List<String> toListOfStrings(SubscriptionObservation observation);
}
