package gov.healthit.chpl.subscription.subject.processor;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;

public class ServiceBaseUrlListChangedActivityProcessor extends SubscriptionSubjectProcessor {
    public static final Long G10_CRITERION_ID = 182L;

    public ServiceBaseUrlListChangedActivityProcessor(SubscriptionSubject subject) {
        super(subject);
    }

    public boolean isRelevantTo(ActivityDTO activity, Object originalData, Object newData) {
        if (activity.getConcept().equals(ActivityConcept.CERTIFIED_PRODUCT)) {
            CertifiedProductSearchDetails originalListing = (CertifiedProductSearchDetails) originalData;
            CertifiedProductSearchDetails newListing = (CertifiedProductSearchDetails) newData;
            return originalListing != null && newListing != null
                    && serviceBaseUrlChanged(originalListing, newListing);
        }
        return false;
    }

    public boolean serviceBaseUrlChanged(CertifiedProductSearchDetails originalListing,
            CertifiedProductSearchDetails newListing) {
        CertificationResult originalG10CertResult = getG10CertResult(originalListing);
        CertificationResult newG10CertResult = getG10CertResult(newListing);
        return originalG10CertResult != null && newG10CertResult != null
                && !StringUtils.equals(originalG10CertResult.getServiceBaseUrlList(), newG10CertResult.getServiceBaseUrlList());

    }

    private CertificationResult getG10CertResult(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(G10_CRITERION_ID))
                .findAny().orElse(null);
    }
}
