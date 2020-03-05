package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("cqmAttestedCriteriaReviewer")
public class CqmAttestedCriteriaReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CqmAttestedCriteriaReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        //any criteria that is applied to a cqm must also be attested to on the listing
        for (CQMResultDetails cqm : listing.getCqmResults()) {
            for (CQMResultCertification cqmCriterion : cqm.getCriteria()) {
                if (!ValidationUtils.hasCert(cqmCriterion.getCertificationNumber(),
                        ValidationUtils.getAttestedCriteria(listing))) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingCriteriaForCqm",
                                    cqm.getCmsId(), cqmCriterion.getCertificationNumber()));
                }
            }
        }
    }
}
