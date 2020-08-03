package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.InvalidCriteriaCombination;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("invalidCriteriaCombinationReviewer")
public class InvalidCriteriaCombinationReviewer extends InvalidCriteriaCombination implements Reviewer {

    @Autowired
    public InvalidCriteriaCombinationReviewer(ErrorMessageUtil msgUtil) {
        super(msgUtil);
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        checkForInvalidCriteriaCombination(listing, getCriteriaB6Id(), getCriteriaB10Id());
        checkForInvalidCriteriaCombination(listing, getCriteriaG8Id(), getCriteriaG10Id());

        initializeOldAndNewCriteriaPairs();
        for (Pair<Integer, Integer> pair : getOldAndNewcriteriaIdPairs()) {
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
            listing.getErrorMessages()
                    .add(getMsgUtil().getMessage("listing.criteria.invalidCombination",
                            CertificationCriterionService.formatCriteriaNumber(critA),
                            CertificationCriterionService.formatCriteriaNumber(critB)));
        }
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing,
            Integer criteriaId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criteriaId)) && cr.isSuccess())
                .findFirst();
    }
}
