package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.InvalidCriteriaCombinationReviewer;

@Component("listingUploadCriteriaReviewer")
public class CriteriaReviewer {

    private RemovedCriteriaReviewer removedCriteriaReviewer;
    private PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer;
    private InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer;

    @Autowired
    public CriteriaReviewer(@Qualifier("listingUploadRemovedCriteriaReviewer") RemovedCriteriaReviewer removedCriteriaReviewer,
            @Qualifier("listingUploadPrivacyAndSecurityCriteriaReviewer") PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer,
            @Qualifier("invalidCriteriaCombinationReviewer") InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer) {
        this.removedCriteriaReviewer = removedCriteriaReviewer;
        this.privacyAndSecurityCriteriaReviewer = privacyAndSecurityCriteriaReviewer;
        this.invalidCriteriaCombinationReviewer = invalidCriteriaCombinationReviewer;
    }

    public void review(CertifiedProductSearchDetails listing) {
        removedCriteriaReviewer.review(listing);
        privacyAndSecurityCriteriaReviewer.review(listing);
        invalidCriteriaCombinationReviewer.review(listing);
    }
}
