package gov.healthit.chpl.subscription.subject.processor;

import gov.healthit.chpl.dto.ActivityDTO;

public interface SubscriptionSubjectProcessor {

    /**
     * Determines if the passed-in activity is relevant to the subscription subject
     */
    boolean isRelevantTo(ActivityDTO activity, Object originalData, Object newData);

    /**
     * Returns the "subject" this processor is observing
     */
    String getSubjectName();
}
