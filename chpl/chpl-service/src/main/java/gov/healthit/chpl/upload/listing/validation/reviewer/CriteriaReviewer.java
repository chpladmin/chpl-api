package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.InvalidCriteriaCombinationReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredAndRelatedCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;

@Component("listingUploadCriteriaReviewer")
public class CriteriaReviewer {

    private RemovedCriteriaReviewer removedCriteriaReviewer;
    private PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer;
    private InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer;
    private RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer;
    private SedG32015Reviewer sedG3Reviewer;

    @Autowired
    public CriteriaReviewer(@Qualifier("listingUploadRemovedCriteriaReviewer") RemovedCriteriaReviewer removedCriteriaReviewer,
            @Qualifier("listingUploadPrivacyAndSecurityCriteriaReviewer") PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer,
            @Qualifier("invalidCriteriaCombinationReviewer") InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer,
            @Qualifier("requiredAndRelatedCriteriaReviewer") RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer,
            @Qualifier("sedG32015Reviewer") SedG32015Reviewer sedG3Reviewer) {
        this.removedCriteriaReviewer = removedCriteriaReviewer;
        this.privacyAndSecurityCriteriaReviewer = privacyAndSecurityCriteriaReviewer;
        this.invalidCriteriaCombinationReviewer = invalidCriteriaCombinationReviewer;
        this.requiredAndRelatedCriteriaReviewer = requiredAndRelatedCriteriaReviewer;
        this.sedG3Reviewer = sedG3Reviewer;
    }

    public void review(CertifiedProductSearchDetails listing) {
        removedCriteriaReviewer.review(listing);
        privacyAndSecurityCriteriaReviewer.review(listing);
        invalidCriteriaCombinationReviewer.review(listing);
        requiredAndRelatedCriteriaReviewer.review(listing);
        sedG3Reviewer.review(listing);
    }
}
