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

    private PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer;
    private InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer;
    private RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer;
    private SedG32015Reviewer sedG3Reviewer;
    private SedRelatedCriteriaReviewer sedRelatedCriteriaReviewer;

    @Autowired
    public CriteriaReviewer(@Qualifier("listingUploadPrivacyAndSecurityCriteriaReviewer") PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer,
            @Qualifier("invalidCriteriaCombinationReviewer") InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer,
            @Qualifier("requiredAndRelatedCriteriaReviewer") RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer,
            SedRelatedCriteriaReviewer sedRelatedCriteriaReviewer) {
        this.privacyAndSecurityCriteriaReviewer = privacyAndSecurityCriteriaReviewer;
        this.invalidCriteriaCombinationReviewer = invalidCriteriaCombinationReviewer;
        this.requiredAndRelatedCriteriaReviewer = requiredAndRelatedCriteriaReviewer;
        this.sedRelatedCriteriaReviewer = sedRelatedCriteriaReviewer;
    }

    public void review(CertifiedProductSearchDetails listing) {
        privacyAndSecurityCriteriaReviewer.review(listing);
        invalidCriteriaCombinationReviewer.review(listing);
        requiredAndRelatedCriteriaReviewer.review(listing);
        sedRelatedCriteriaReviewer.review(listing);
    }
}
