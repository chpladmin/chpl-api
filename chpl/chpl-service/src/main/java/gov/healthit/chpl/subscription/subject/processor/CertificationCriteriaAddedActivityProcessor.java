package gov.healthit.chpl.subscription.subject.processor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;

public class CertificationCriteriaAddedActivityProcessor extends SubscriptionSubjectProcessor {

    public CertificationCriteriaAddedActivityProcessor(SubscriptionSubject subject) {
        super(subject);
    }

    public boolean isRelevantTo(ActivityDTO activity, Object originalData, Object newData) {
        if (activity.getConcept().equals(ActivityConcept.CERTIFIED_PRODUCT)) {
            CertifiedProductSearchDetails originalListing = (CertifiedProductSearchDetails) originalData;
            CertifiedProductSearchDetails newListing = (CertifiedProductSearchDetails) newData;
            return originalListing != null && newListing != null
                    && attestsToAnyDifferentCriteria(originalListing, newListing);
        }
        return false;
    }

    private boolean attestsToAnyDifferentCriteria(CertifiedProductSearchDetails originalListing, CertifiedProductSearchDetails newListing) {
        List<Pair<CertificationResult, CertificationResult>> origAndNewCertResultPairs
            = newListing.getCertificationResults().stream()
                .map(newCertResult -> createCertResultPair(originalListing.getCertificationResults(), newCertResult))
                .collect(Collectors.toList());
        return origAndNewCertResultPairs.stream()
            .anyMatch(pair -> (pair.getLeft() == null || BooleanUtils.isFalse(pair.getLeft().getSuccess()))
                                    && (pair.getRight() != null && BooleanUtils.isTrue(pair.getRight().getSuccess())));
    }

    private Pair<CertificationResult, CertificationResult> createCertResultPair(List<CertificationResult> origCertResults, CertificationResult newCertResult) {
        Optional<CertificationResult> origCertResult = origCertResults.stream()
                .filter(origCr -> origCr.getCriterion().getId().equals(newCertResult.getCriterion().getId()))
                .findAny();
        if (origCertResult.isEmpty()) {
            return Pair.of(null, newCertResult);
        }
        return Pair.of(origCertResult.get(), newCertResult);
    }
}
