package gov.healthit.chpl.subscription.subject.processor;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;

public class RwtPlansUrlChangedActivityProcessor extends SubscriptionSubjectProcessor {

    public RwtPlansUrlChangedActivityProcessor(SubscriptionSubject subject) {
        super(subject);
    }

    public boolean isRelevantTo(ActivityDTO activity, Object originalData, Object newData) {
        if (activity.getConcept().equals(ActivityConcept.CERTIFIED_PRODUCT)) {
            CertifiedProductSearchDetails originalListing = (CertifiedProductSearchDetails) originalData;
            CertifiedProductSearchDetails newListing = (CertifiedProductSearchDetails) newData;
            return originalListing != null && newListing != null
                    && !StringUtils.equals(originalListing.getRwtPlansUrl(), newListing.getRwtPlansUrl());
        }
        return false;
    }
}
