package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.InvalidCriteriaCombination;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingInvalidCriteriaCombinationReviewer")
public class InvalidCriteriaCombinationReviewer extends InvalidCriteriaCombination implements Reviewer {
    @Autowired
    public InvalidCriteriaCombinationReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        super(msgUtil, ff4j);
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
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

    private void checkForInvalidCriteriaCombination(PendingCertifiedProductDTO listing, Integer criteriaIdA,
            Integer criteriaIdB) {
        Optional<PendingCertificationResultDTO> certResultA = findCertificationResult(listing, criteriaIdA);
        Optional<PendingCertificationResultDTO> certResultB = findCertificationResult(listing, criteriaIdB);

        if (certResultA.isPresent() && certResultB.isPresent()) {
            final CertificationCriterionDTO critA = certResultA.get().getCriterion();
            final CertificationCriterionDTO critB = certResultB.get().getCriterion();
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.criteria.invalidCombination",
                            Util.formatCriteriaNumber(critA), Util.formatCriteriaNumber(critB)));
        }
    }

    private Optional<PendingCertificationResultDTO> findCertificationResult(PendingCertifiedProductDTO listing,
            Integer criteriaId) {
        return listing.getCertificationCriterion().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criteriaId)) && cr.getMeetsCriteria())
                .findFirst();
    }
}
