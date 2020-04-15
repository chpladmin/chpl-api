package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.InvalidCriteriaCombination;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("invalidCriteriaCombinationReviewer")
public class InvalidCriteriaCombinationReviewer extends InvalidCriteriaCombination implements Reviewer {
    private CertificationCriterionService criterionService;

    @Autowired
    public InvalidCriteriaCombinationReviewer(ErrorMessageUtil msgUtil, FF4j ff4j,
            CertificationCriterionService criterionService) {
        super(msgUtil, ff4j);
        this.criterionService = criterionService;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            checkForInvalidCriteriaCombination(listing, criteriaB6Id, criteriaB10Id);
            checkForInvalidCriteriaCombination(listing, criteriaG8Id, criteriaG10Id);

            initializeOldAndNewCriteriaPairs();
            for (Pair<Integer, Integer> pair : oldAndNewcriteriaIdPairs) {
                final Integer oldCriteriaId = pair.getLeft();
                final Integer newCriteriaId = pair.getRight();
                checkForInvalidCriteriaCombination(listing, oldCriteriaId, newCriteriaId);
            }
        }
    }

    private void checkForInvalidCriteriaCombination(CertifiedProductSearchDetails listing, Integer criteriaIdA,
            Integer criteriaIdB) {
        Optional<CertificationResult> certResultA = findCertificationResult(listing, criteriaIdA);
        Optional<CertificationResult> certResultB = findCertificationResult(listing, criteriaIdB);

        if (certResultA.isPresent() && certResultB.isPresent()) {
            final CertificationCriterion critA = certResultA.get().getCriterion();
            final CertificationCriterion critB = certResultB.get().getCriterion();
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.criteria.invalidCombination",
                            criterionService.formatCriteriaNumber(critA), criterionService.formatCriteriaNumber(critB)));
        }
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing,
            Integer criteriaId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criteriaId)) && cr.isSuccess())
                .findFirst();
    }
}
