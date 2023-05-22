package gov.healthit.chpl.subscription.subject.processor;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

public class CertificationStatusChangedActivityProcessor implements SubscriptionSubjectProcessor {
    private static final String SUBJECT_NAME = "Certification Status Changed";

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

    public String getSubjectName() {
        return SUBJECT_NAME;
    }
}
