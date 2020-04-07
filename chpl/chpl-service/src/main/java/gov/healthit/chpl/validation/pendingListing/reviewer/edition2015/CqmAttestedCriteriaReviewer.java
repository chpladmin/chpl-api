package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCriterionDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingCqmAttestedCriteriaReviewer")
public class CqmAttestedCriteriaReviewer implements Reviewer {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CqmAttestedCriteriaReviewer(ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        //any criteria that is applied to a cqm must also be attested to on the listing
        for (PendingCqmCriterionDTO cqm : listing.getCqmCriterion()) {
            for (PendingCqmCertificationCriterionDTO cqmCriterion : cqm.getCertifications()) {
                if (!validationUtils.hasCert(cqmCriterion.getCertificationCriteriaNumber(),
                        validationUtils.getAttestedCriteria(listing))) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingCriteriaForCqm",
                                    cqm.getCmsId(), cqmCriterion.getCertificationCriteriaNumber()));
                }
            }
        }
    }
}
