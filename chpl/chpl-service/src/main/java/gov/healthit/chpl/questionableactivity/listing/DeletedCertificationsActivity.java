package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class DeletedCertificationsActivity implements ListingActivity {

    private CertificationCriterionService criteriaService;
    private Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap;

    @Autowired
    public DeletedCertificationsActivity(CertificationCriterionService criteriaService) {
        this.criteriaService = criteriaService;

        originalToCuresCriteriaMap = criteriaService.getOriginalToCuresCriteriaMap();
    }

    @Override
     public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListing> certRemovedActivities = new ArrayList<QuestionableActivityListing>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            List<CertificationCriterion> removedCriteria = getRemovedCriteria(origListing, newListing);
            removedCriteria.stream()
                .filter(removedCriterion -> !wasCuresCriteriaSwappedForOriginal(removedCriterion, origListing, newListing))
                .forEach(removedCriterionWithoutCuresAdded -> {
                    QuestionableActivityListing activity = new QuestionableActivityListing();
                    activity.setBefore(CertificationCriterionService.formatCriteriaNumber(removedCriterionWithoutCuresAdded));
                    activity.setAfter(null);
                    certRemovedActivities.add(activity);
                });
        }
        return certRemovedActivities;
    }

    private List<CertificationCriterion> getRemovedCriteria(CertifiedProductSearchDetails originalListing, CertifiedProductSearchDetails newListing) {
        List<Pair<CertificationResult, CertificationResult>> origAndNewCertResultPairs
            = originalListing.getCertificationResults().stream()
                .map(origCertResult -> createCertResultPair(origCertResult, newListing.getCertificationResults()))
                .collect(Collectors.toList());
        return origAndNewCertResultPairs.stream()
            .filter(pair -> (pair.getLeft() != null && BooleanUtils.isTrue(pair.getLeft().isSuccess()))
                                && (pair.getRight() == null || BooleanUtils.isFalse(pair.getRight().isSuccess())))
            .map(pair -> pair.getLeft().getCriterion())
            .collect(Collectors.toList());
    }

    private boolean wasCuresCriteriaSwappedForOriginal(CertificationCriterion removedCriterion,
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        removedCriterion = criteriaService.get(removedCriterion.getId());
        Set<CertificationCriterion> originalCriteria = originalToCuresCriteriaMap.keySet();
        return originalCriteria.contains(removedCriterion)
                && wasCuresCounterpartAdded(removedCriterion, origListing, newListing);
    }

    private boolean wasCuresCounterpartAdded(CertificationCriterion removedCriterion,
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        CertificationCriterion curesCounterpart = originalToCuresCriteriaMap.get(removedCriterion);
        if (curesCounterpart == null) {
            return false;
        }
        List<CertificationCriterion> newlyAttestedCriteria = getNewlyAttestedCriteria(origListing, newListing);
        return newlyAttestedCriteria.stream()
                .filter(crit -> crit.getId().equals(curesCounterpart.getId()))
                .findAny()
                .isPresent();
    }

    private List<CertificationCriterion> getNewlyAttestedCriteria(CertifiedProductSearchDetails originalListing, CertifiedProductSearchDetails newListing) {
        List<Pair<CertificationResult, CertificationResult>> origAndNewCertResultPairs
            = newListing.getCertificationResults().stream()
                .map(newCertResult -> createCertResultPair(originalListing.getCertificationResults(), newCertResult))
                .collect(Collectors.toList());
        return origAndNewCertResultPairs.stream()
            .filter(pair -> (pair.getLeft() == null || BooleanUtils.isFalse(pair.getLeft().isSuccess()))
                                    && (pair.getRight() != null && BooleanUtils.isTrue(pair.getRight().isSuccess())))
            .map(pair -> pair.getRight().getCriterion())
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

    private Pair<CertificationResult, CertificationResult> createCertResultPair(List<CertificationResult> origCertResults, CertificationResult newCertResult) {
        Optional<CertificationResult> origCertResult = origCertResults.stream()
                .filter(origCr -> origCr.getCriterion().getId().equals(newCertResult.getCriterion().getId()))
                .findAny();
        if (origCertResult.isEmpty()) {
            return Pair.of(null, newCertResult);
        }
        return Pair.of(origCertResult.get(), newCertResult);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CRITERIA_REMOVED;
    }

}
