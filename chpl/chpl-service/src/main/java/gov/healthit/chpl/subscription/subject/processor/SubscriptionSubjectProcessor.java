package gov.healthit.chpl.subscription.subject.processor;

import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;

public abstract class SubscriptionSubjectProcessor {

    private SubscriptionSubject subject;

    public SubscriptionSubjectProcessor(SubscriptionSubject subject) {
        this.subject = subject;
    }

    public SubscriptionSubject getSubject() {
        return subject;
    }

    /**
     * Determines if the passed-in activity is relevant to the subscription subject
     */
    public abstract boolean isRelevantTo(ActivityDTO activity, Object originalData, Object newData);
}
