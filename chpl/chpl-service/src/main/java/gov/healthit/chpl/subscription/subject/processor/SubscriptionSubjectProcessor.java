package gov.healthit.chpl.subscription.subject.processor;

import gov.healthit.chpl.dto.ActivityDTO;

public interface SubscriptionSubjectProcessor {

    boolean doesActivityMatchSubject(ActivityDTO activity, Object originalData, Object newData);
    String displayActivity(); //used by a system job when creating emails with consolidated observations
    String getSubjectName();
}
