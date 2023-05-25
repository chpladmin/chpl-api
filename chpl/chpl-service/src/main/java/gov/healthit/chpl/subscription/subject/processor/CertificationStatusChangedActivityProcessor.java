package gov.healthit.chpl.subscription.subject.processor;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;

public class CertificationStatusChangedActivityProcessor extends SubscriptionSubjectProcessor {

    public CertificationStatusChangedActivityProcessor(SubscriptionSubject subject) {
        super(subject);
    }

    public boolean isRelevantTo(ActivityDTO activity, Object originalData, Object newData) {
        if (activity.getConcept().equals(ActivityConcept.CERTIFIED_PRODUCT)) {
            CertifiedProductSearchDetails originalListing = (CertifiedProductSearchDetails) originalData;
            CertifiedProductSearchDetails newListing = (CertifiedProductSearchDetails) newData;
            return areCertificationStatusesDifferent(originalListing, newListing);
        }
        return false;
    }

    private boolean areCertificationStatusesDifferent(CertifiedProductSearchDetails originalListing,
            CertifiedProductSearchDetails newListing) {
        return !originalListing.getCurrentStatus().getStatus().getId().equals(newListing.getCurrentStatus().getStatus().getId());
    }
}
