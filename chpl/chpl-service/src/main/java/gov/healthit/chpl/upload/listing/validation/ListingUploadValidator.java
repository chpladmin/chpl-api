package gov.healthit.chpl.upload.listing.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.validation.reviewer.AdditionalSoftwareCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CSVHeaderReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertifiedDateCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ChplNumberFormatReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ChplNumberUniqueReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.EditionCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.IcsCodeReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritanceReviewer;

@Component
public class ListingUploadValidator {
    private CSVHeaderReviewer csvHeaderReviewer;
    private ChplNumberFormatReviewer chplNumberFormatReviewer;
    private EditionCodeReviewer editionCodeReviewer;
    //TODO: ATL Code, ACB Code, Developer code reviewers
    private IcsCodeReviewer icsCodeReviewer;
    private AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer;
    private CertifiedDateCodeReviewer certifiedDateCodeReviewer;

    private ChplNumberUniqueReviewer chplNumberUniqueReviewer;
    private CertificationDateReviewer certDateReviewer;
    //TODO: needs unit test
    private DeveloperStatusReviewer devStatusReviewer;
    //TODO: needs unit test
    private InheritanceReviewer inheritanceReviewer;


    @Autowired
    public ListingUploadValidator(CSVHeaderReviewer csvHeaderReviewer,
            ChplNumberFormatReviewer chplNumberFormatReviewer,
            EditionCodeReviewer editionCodeReviewer,
            IcsCodeReviewer icsCodeReviewer,
            AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer,
            CertifiedDateCodeReviewer certifiedDateCodeReviewer,
            CertificationDateReviewer certDateReviewer,
            ChplNumberUniqueReviewer chplNumberUniqueReviewer,
            DeveloperStatusReviewer devStatusReviewer) {
        this.csvHeaderReviewer = csvHeaderReviewer;
        this.chplNumberFormatReviewer = chplNumberFormatReviewer;
        this.editionCodeReviewer = editionCodeReviewer;
        this.icsCodeReviewer = icsCodeReviewer;
        this.additionalSoftwareCodeReviewer = additionalSoftwareCodeReviewer;
        this.certifiedDateCodeReviewer = certifiedDateCodeReviewer;
        this.certDateReviewer = certDateReviewer;
        this.chplNumberUniqueReviewer = chplNumberUniqueReviewer;
        this.devStatusReviewer = devStatusReviewer;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        csvHeaderReviewer.review(uploadedMetadata, listing);
        chplNumberFormatReviewer.review(listing);
        editionCodeReviewer.review(listing);
        icsCodeReviewer.review(listing);
        additionalSoftwareCodeReviewer.review(listing);
        certifiedDateCodeReviewer.review(listing);
        certDateReviewer.review(listing);
        chplNumberUniqueReviewer.review(listing);
        devStatusReviewer.review(listing);
        inheritanceReviewer.review(listing);
    }
}
