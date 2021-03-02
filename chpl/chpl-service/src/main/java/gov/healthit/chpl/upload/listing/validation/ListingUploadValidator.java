package gov.healthit.chpl.upload.listing.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;

@Component
public class ListingUploadValidator {
    //CSV Reviewers that review the ListingUpload domain object and look for unrecognized columns to warn the user
    private CSVHeaderReviewer csvHeaderReviewer;

    //Reviewers for newly uploaded listings (no comparison reviewers because nothing is changing as with edits)

    @Autowired
    public ListingUploadValidator(CSVHeaderReviewer csvHeaderReviewer) {
        this.csvHeaderReviewer = csvHeaderReviewer;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        csvHeaderReviewer.review(uploadedMetadata, listing);
    }
}
