package gov.healthit.chpl.upload.listing.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.validation.reviewer.AdditionalSoftwareCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CSVHeaderReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertificationBodyCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertificationBodyReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertifiedDateCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ChplNumberFormatReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ChplNumberUniqueReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.EditionCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.EditionReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.IcsCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.TestingLabCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.TestingLabReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritanceReviewer;

@Component
public class ListingUploadValidator {
    private CSVHeaderReviewer csvHeaderReviewer;
    private ChplNumberFormatReviewer chplNumberFormatReviewer;
    private EditionCodeReviewer editionCodeReviewer;
    private TestingLabCodeReviewer atlCodeReviewer;
    private CertificationBodyCodeReviewer acbCodeReviewer;
    //TODO: Developer code reviewers
    private IcsCodeReviewer icsCodeReviewer;
    private AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer;
    private CertifiedDateCodeReviewer certifiedDateCodeReviewer;

    private ChplNumberUniqueReviewer chplNumberUniqueReviewer;
    private EditionReviewer editionReviewer;
    private TestingLabReviewer atlReviewer;
    private CertificationBodyReviewer acbReviewer;
    //TODO: Developer reviewers
    private CertificationDateReviewer certDateReviewer;
    //TODO: needs unit test
    private DeveloperStatusReviewer devStatusReviewer;
    //TODO: needs unit test
    private InheritanceReviewer inheritanceReviewer;


    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingUploadValidator(CSVHeaderReviewer csvHeaderReviewer,
            ChplNumberFormatReviewer chplNumberFormatReviewer,
            EditionCodeReviewer editionCodeReviewer,
            TestingLabCodeReviewer atlCodeReviewer,
            CertificationBodyCodeReviewer acbCodeReviewer,
            IcsCodeReviewer icsCodeReviewer,
            AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer,
            EditionReviewer editionReviewer,
            TestingLabReviewer atlReviewer,
            CertificationBodyReviewer acbReviewer,
            CertifiedDateCodeReviewer certifiedDateCodeReviewer,
            CertificationDateReviewer certDateReviewer,
            ChplNumberUniqueReviewer chplNumberUniqueReviewer,
            DeveloperStatusReviewer devStatusReviewer) {
        this.csvHeaderReviewer = csvHeaderReviewer;
        this.chplNumberFormatReviewer = chplNumberFormatReviewer;
        this.editionCodeReviewer = editionCodeReviewer;
        this.atlCodeReviewer = atlCodeReviewer;
        this.acbCodeReviewer = acbCodeReviewer;
        this.icsCodeReviewer = icsCodeReviewer;
        this.additionalSoftwareCodeReviewer = additionalSoftwareCodeReviewer;
        this.certifiedDateCodeReviewer = certifiedDateCodeReviewer;
        this.editionReviewer = editionReviewer;
        this.atlReviewer = atlReviewer;
        this.acbReviewer = acbReviewer;
        this.certDateReviewer = certDateReviewer;
        this.chplNumberUniqueReviewer = chplNumberUniqueReviewer;
        this.devStatusReviewer = devStatusReviewer;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        csvHeaderReviewer.review(uploadedMetadata, listing);
        chplNumberFormatReviewer.review(listing);
        editionCodeReviewer.review(listing);
        atlCodeReviewer.review(listing);
        acbCodeReviewer.review(listing);
        icsCodeReviewer.review(listing);
        additionalSoftwareCodeReviewer.review(listing);
        certifiedDateCodeReviewer.review(listing);
        editionReviewer.review(listing);
        atlReviewer.review(listing);
        acbReviewer.review(listing);
        certDateReviewer.review(listing);
        chplNumberUniqueReviewer.review(listing);
        devStatusReviewer.review(listing);
        inheritanceReviewer.review(listing);
    }
}
