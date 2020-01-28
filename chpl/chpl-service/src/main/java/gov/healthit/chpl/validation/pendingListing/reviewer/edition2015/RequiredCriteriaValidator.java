package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingRequiredCriteriaValidator")
public class RequiredCriteriaValidator implements Reviewer {

    @Value("${criterion.170_315_d_12}")
    private Integer criteriaD12Id;

    @Value("${criterion.170_315_d_13}")
    private Integer criteriaD13Id;

    private ErrorMessageUtil msgUtil;

    @Autowired
    public RequiredCriteriaValidator(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        checkRequiredCriterionExist(listing, criteriaD12Id);
        checkRequiredCriterionExist(listing, criteriaD13Id);
    }

    private void checkRequiredCriterionExist(PendingCertifiedProductDTO listing, Integer criterionId) {
        Optional<PendingCertificationResultDTO> certResult = findCertificationResult(listing, criterionId);
        if (certResult.isPresent() && !certResult.get().getMeetsCriteria()) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.required", certResult.get().getCriterion().getNumber()));

        }
    }

    private Optional<PendingCertificationResultDTO> findCertificationResult(PendingCertifiedProductDTO listing,
            Integer criterionId) {
        return listing.getCertificationCriterion().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criterionId)))
                .findFirst();
    }
}
