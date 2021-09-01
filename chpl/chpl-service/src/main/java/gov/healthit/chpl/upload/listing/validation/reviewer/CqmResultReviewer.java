package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.AttestedCriteriaCqmReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.CqmAttestedCriteriaReviewer;

@Component
public class CqmResultReviewer implements Reviewer {
    private CqmAttestedCriteriaReviewer cqmAttestedCriteriaReviewer;
    private AttestedCriteriaCqmReviewer attestedCriteriaCqmReviewer;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public CqmResultReviewer(@Qualifier("cqmAttestedCriteriaReviewer") CqmAttestedCriteriaReviewer cqmAttestedCriteriaReviewer,
            @Qualifier("attestedCriteriaCqmReviewer") AttestedCriteriaCqmReviewer attestedCriteriaCqmReviewer,
            ErrorMessageUtil msgUtil) {
        this.cqmAttestedCriteriaReviewer = cqmAttestedCriteriaReviewer;
        this.attestedCriteriaCqmReviewer = attestedCriteriaCqmReviewer;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        //TODO: CQM + Version doesn't exist = error
        //TODO: If Version is missing current behavior is it acts as if that CQM isnt listed/attested, i feel like this should be a warning at minimum
        cqmAttestedCriteriaReviewer.review(listing);
        attestedCriteriaCqmReviewer.review(listing);
    }
}
