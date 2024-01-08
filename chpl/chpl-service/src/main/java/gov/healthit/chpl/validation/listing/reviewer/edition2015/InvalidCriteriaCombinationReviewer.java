package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.InvalidCriteriaCombination;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("invalidCriteriaCombinationReviewer")
public class InvalidCriteriaCombinationReviewer implements Reviewer {
    private InvalidCriteriaCombination invalidCriteriaCombination;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public InvalidCriteriaCombinationReviewer(InvalidCriteriaCombination invalidCriteriaCombination,
            ErrorMessageUtil msgUtil) {
        this.invalidCriteriaCombination = invalidCriteriaCombination;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        for (Pair<Integer, Integer> pair : invalidCriteriaCombination.getOldAndNewcriteriaIdPairs()) {
            final Integer oldCriteriaId = pair.getLeft();
            final Integer newCriteriaId = pair.getRight();
            checkForInvalidCriteriaCombination(listing, oldCriteriaId, newCriteriaId);
        }
    }

    private void checkForInvalidCriteriaCombination(CertifiedProductSearchDetails listing, Integer criteriaIdA,
            Integer criteriaIdB) {
        Optional<CertificationResult> certResultA = findCertificationResult(listing, criteriaIdA);
        Optional<CertificationResult> certResultB = findCertificationResult(listing, criteriaIdB);

        if (certResultA.isPresent() && certResultB.isPresent()) {
            final CertificationCriterion critA = certResultA.get().getCriterion();
            final CertificationCriterion critB = certResultB.get().getCriterion();
            listing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidCombination",
                            CertificationCriterionService.formatCriteriaNumber(critA),
                            CertificationCriterionService.formatCriteriaNumber(critB)));
        }
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing,
            Integer criteriaId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> BooleanUtils.isTrue(cr.getSuccess())
                        && cr.getCriterion() != null && cr.getCriterion().getId() != null
                        && cr.getCriterion().getId().equals(Long.valueOf(criteriaId)))
                .findFirst();
    }
}
