package gov.healthit.chpl.validation.listing;

import java.util.List;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class Validator {

    public abstract List<Reviewer> getReviewers();

    public abstract List<ComparisonReviewer> getComparisonReviewers();

    public synchronized void validate(CertifiedProductSearchDetails listing) {
        if (listing.isCertificateActive()) {
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
        if (updatedListing.isCertificateActive()) {
            validate(updatedListing);
            for (ComparisonReviewer reviewer : getComparisonReviewers()) {
                reviewer.review(existingListing, updatedListing);
            }
        }
    }
}
