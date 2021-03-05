package gov.healthit.chpl.upload.listing.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.validation.reviewer.AdditionalSoftwareCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CSVHeaderReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertifiedDateCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ChplNumberUniqueReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritanceReviewer;

@Component
public class ListingUploadValidator {
    //CSV Reviewers that review the ListingUpload domain object and look for unrecognized columns to warn the user
    private CSVHeaderReviewer csvHeaderReviewer;

    //Reviewers for newly uploaded listings (no comparison reviewers because nothing is changing as with edits)
    private AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer;
    private CertifiedDateCodeReviewer certifiedDateCodeReviewer;
    private CertificationDateReviewer certDateReviewer;
    private ChplNumberUniqueReviewer chplNumberUniqueReviewer;

    //these need unit tests added
    private DeveloperStatusReviewer devStatusReviewer;
    private InheritanceReviewer inheritanceReviewer;


    @Autowired
    public ListingUploadValidator(CSVHeaderReviewer csvHeaderReviewer,
            AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer,
            CertifiedDateCodeReviewer certifiedDateCodeReviewer,
            CertificationDateReviewer certDateReviewer,
            ChplNumberUniqueReviewer chplNumberUniqueReviewer,
            DeveloperStatusReviewer devStatusReviewer) {
        this.csvHeaderReviewer = csvHeaderReviewer;
        this.additionalSoftwareCodeReviewer = additionalSoftwareCodeReviewer;
        this.certifiedDateCodeReviewer = certifiedDateCodeReviewer;
        this.certDateReviewer = certDateReviewer;
        this.chplNumberUniqueReviewer = chplNumberUniqueReviewer;
        this.devStatusReviewer = devStatusReviewer;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        csvHeaderReviewer.review(uploadedMetadata, listing);
        additionalSoftwareCodeReviewer.review(listing);
        certifiedDateCodeReviewer.review(listing);
        certDateReviewer.review(listing);
        chplNumberUniqueReviewer.review(listing);
        devStatusReviewer.review(listing);
    }
}
