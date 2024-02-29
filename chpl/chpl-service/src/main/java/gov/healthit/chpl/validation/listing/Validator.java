package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class Validator {

    public abstract List<Reviewer> getReviewers();
    public abstract List<ComparisonReviewer> getComparisonReviewers();
    public abstract List<ComparisonReviewer> getComparisonReviewersToAlwaysCheck();

    public synchronized void validate(CertifiedProductSearchDetails listing) {
        if (CollectionUtils.isEmpty(listing.getCertificationEvents()) || listing.isCertificateActive()) {
            for (Reviewer reviewer : getReviewers()) {
                try {
                    if (reviewer != null) {
                        reviewer.review(listing);
                    } else {
                        LOGGER.info("Cound not run a NULL reviewer.");
                    }
                } catch (Exception e) {
                    LOGGER.error("There was an exception trying to run the Reviewer: " + reviewer.getClass().getName());
                    throw e;
                }
            }
        }
    }

    public void validate(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        //validation only runs if the listing is Active. A user must be able to set a listing with errors to Withdrawn
        //without having to make changes to get it to be "valid"
        if (CollectionUtils.isEmpty(updatedListing.getCertificationEvents()) || updatedListing.isCertificateActive()) {
            validate(updatedListing);
            for (ComparisonReviewer reviewer : getComparisonReviewers()) {
                reviewer.review(existingListing, updatedListing);
            }
        }

        //but there are some reviewers we need to always run, no matter the listing status
        for (ComparisonReviewer reviewer : getComparisonReviewersToAlwaysCheck()) {
            reviewer.review(existingListing, updatedListing);
        }
    }
}
