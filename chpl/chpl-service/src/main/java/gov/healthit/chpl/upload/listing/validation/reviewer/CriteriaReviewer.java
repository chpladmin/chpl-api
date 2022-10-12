package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.InvalidCriteriaCombinationReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredAndRelatedCriteriaErdPhase2GracePeriodReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredAndRelatedCriteriaPreErdPhase2Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredAndRelatedCriteriaReviewer;

@Component("listingUploadCriteriaReviewer")
public class CriteriaReviewer {

    private PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer;
    private InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer;
    private RequiredAndRelatedCriteriaPreErdPhase2Reviewer requiredAndRelatedCriteriaPreErdPhase2Reviewer;
    private RequiredAndRelatedCriteriaErdPhase2GracePeriodReviewer requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer;
    private RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer;
    private SedRelatedCriteriaReviewer sedRelatedCriteriaReviewer;
    private FF4j ff4j;

    @Autowired
    public CriteriaReviewer(@Qualifier("privacyAndSecurityCriteriaReviewer") PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer,
            @Qualifier("invalidCriteriaCombinationReviewer") InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer,
            @Qualifier("requiredAndRelatedCriteriaPreErdPhase2Reviewer") RequiredAndRelatedCriteriaPreErdPhase2Reviewer requiredAndRelatedCriteriaPreErdPhase2Reviewer,
            @Qualifier("requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer") RequiredAndRelatedCriteriaErdPhase2GracePeriodReviewer requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer,
            @Qualifier("requiredAndRelatedCriteriaReviewer") RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer,
            SedRelatedCriteriaReviewer sedRelatedCriteriaReviewer,
            FF4j ff4j) {
        this.privacyAndSecurityCriteriaReviewer = privacyAndSecurityCriteriaReviewer;
        this.invalidCriteriaCombinationReviewer = invalidCriteriaCombinationReviewer;
        this.requiredAndRelatedCriteriaPreErdPhase2Reviewer = requiredAndRelatedCriteriaPreErdPhase2Reviewer;
        this.requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer = requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer;
        this.requiredAndRelatedCriteriaReviewer = requiredAndRelatedCriteriaReviewer;
        this.sedRelatedCriteriaReviewer = sedRelatedCriteriaReviewer;
        this.ff4j = ff4j;
    }

    public void review(CertifiedProductSearchDetails listing) {
        privacyAndSecurityCriteriaReviewer.review(listing);
        invalidCriteriaCombinationReviewer.review(listing);
        if (ff4j.check(FeatureList.ERD_PHASE_2)) {
            //use this reviewer during and after the grace period
            requiredAndRelatedCriteriaReviewer.review(listing);
        } else {
            //use this reviewer before ERD-Phase-2
            requiredAndRelatedCriteriaPreErdPhase2Reviewer.review(listing);
        }

        sedRelatedCriteriaReviewer.review(listing);
    }
}
