package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.Optional;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingInvalidCriteriaCombinationReviewer")
public class InvalidCriteriaCombinationReviewer implements Reviewer {

    @Value("${criterion.170_315_b_6}")
    private Integer criteriaB6Id;

    @Value("${criterion.170_315_b_10}")
    private Integer criteriaB10Id;

    @Value("${criterion.170_315_g_8}")
    private Integer criteriaG8Id;

    @Value("${criterion.170_315_g_10}")
    private Integer criteriaG10Id;

    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public InvalidCriteriaCombinationReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            checkForInvalidCriteriaCombination(listing, criteriaB6Id, criteriaB10Id);
            checkForInvalidCriteriaCombination(listing, criteriaG8Id, criteriaG10Id);
        }
    }

    private void checkForInvalidCriteriaCombination(PendingCertifiedProductDTO listing, Integer criteriaIdA,
            Integer criteriaIdB) {
        Optional<PendingCertificationResultDTO> certResultA = findCerificationResult(listing, criteriaIdA);
        Optional<PendingCertificationResultDTO> certResultB = findCerificationResult(listing, criteriaIdB);

        if (certResultA.isPresent() && certResultB.isPresent()) {
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.criteria.invalidCombination", certResultA.get().getCriterion().getNumber(),
                            certResultB.get().getCriterion().getNumber()));
        }
    }

    private Optional<PendingCertificationResultDTO> findCerificationResult(PendingCertifiedProductDTO listing,
            Integer criteriaId) {
        return listing.getCertificationCriterion().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criteriaId)) && cr.getMeetsCriteria())
                .findFirst();
    }
}
