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
            = originalListing.getCertificationResults().stream()
                .map(origCertResult -> createCertResultPair(origCertResult, newListing.getCertificationResults()))
                .collect(Collectors.toList());
        return origAndNewCertResultPairs.stream()
            .anyMatch(pair -> BooleanUtils.isFalse(pair.getLeft().isSuccess()) && BooleanUtils.isTrue(pair.getRight().isSuccess()));
    }

    private Pair<CertificationResult, CertificationResult> createCertResultPair(CertificationResult origCertResult, List<CertificationResult> newCertResults) {
        Optional<CertificationResult> newCertResult = newCertResults.stream()
                .filter(newCr -> newCr.getCriterion().getId().equals(origCertResult.getCriterion().getId()))
                .findAny();
        if (newCertResult.isEmpty()) {
            return Pair.of(origCertResult, null);
        }
        return Pair.of(origCertResult, newCertResult.get());
    }
}
