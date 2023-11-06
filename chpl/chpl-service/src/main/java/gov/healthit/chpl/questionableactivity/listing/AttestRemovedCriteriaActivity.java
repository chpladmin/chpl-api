package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationStatusUtil;

@Component
public class AttestRemovedCriteriaActivity implements ListingActivity {

    @Override
     public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListing> certAddedActivities = new ArrayList<QuestionableActivityListing>();
        if (CertificationStatusUtil.isActive(origListing)
                && origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            List<CertificationCriterion> addedCriteria = getAddedCriteria(origListing, newListing);
            addedCriteria.stream()
                .filter(addedCriterion -> addedCriterion.isRemoved())
                .forEach(removedAddedCriterion -> {
                    QuestionableActivityListing activity = new QuestionableActivityListing();
                    activity.setBefore(CertificationCriterionService.formatCriteriaNumber(removedAddedCriterion));
                    activity.setAfter(null);
                    certAddedActivities.add(activity);
                });
        }
        return certAddedActivities;
    }

    private List<CertificationCriterion> getAddedCriteria(CertifiedProductSearchDetails originalListing, CertifiedProductSearchDetails newListing) {
        List<Pair<CertificationResult, CertificationResult>> origAndNewCertResultPairs
            = originalListing.getCertificationResults().stream()
                .map(origCertResult -> createCertResultPair(origCertResult, newListing.getCertificationResults()))
                .collect(Collectors.toList());
        return origAndNewCertResultPairs.stream()
            .filter(pair -> (pair.getRight() != null && BooleanUtils.isTrue(pair.getRight().isSuccess()))
                                && (pair.getLeft() == null || BooleanUtils.isFalse(pair.getLeft().isSuccess())))
            .map(pair -> pair.getLeft().getCriterion())
            .collect(Collectors.toList());
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

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REMOVED_CRITERIA_ADDED;
    }

}
