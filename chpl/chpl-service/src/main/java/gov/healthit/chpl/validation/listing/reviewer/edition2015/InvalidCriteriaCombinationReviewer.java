package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.Iterator;
import java.util.Optional;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.InvalidCriteriaCombination;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("invalidCriteriaCombinationReviewer")
public class InvalidCriteriaCombinationReviewer extends InvalidCriteriaCombination implements Reviewer {
    @Autowired
    public InvalidCriteriaCombinationReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        super(msgUtil, ff4j);
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            checkForInvalidCriteriaCombination(listing, criteriaB6Id, criteriaB10Id);
            checkForInvalidCriteriaCombination(listing, criteriaG8Id, criteriaG10Id);

            initializeOldAndNewCriteria();
            Iterator<Integer> oldCriteriaIdsIter = oldCriteriaIds.iterator();
            Iterator<Integer> newCriteriaIdsIter = newCriteriaIds.iterator();
            while (oldCriteriaIdsIter.hasNext() && newCriteriaIdsIter.hasNext()) {
                checkForInvalidCriteriaCombination(listing, oldCriteriaIdsIter.next(), newCriteriaIdsIter.next());
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
                            getTitleForErrorMessage(critA), getTitleForErrorMessage(critB)));
        }
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing,
            Integer criteriaId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criteriaId)) && cr.isSuccess())
                .findFirst();
    }

    private String getTitleForErrorMessage(CertificationCriterion criterion) {
        return criterion.getTitle().contains(CURES_UPDATE_IN_TITLE)
                ? criterion.getNumber() + " " + CURES_UPDATE_IN_TITLE
                : criterion.getNumber();
    }
}
