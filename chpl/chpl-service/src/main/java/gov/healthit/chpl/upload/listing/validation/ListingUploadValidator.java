package gov.healthit.chpl.upload.listing.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import gov.healthit.chpl.upload.listing.validation.reviewer.DeveloperCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.DeveloperReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.EditionCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.EditionReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.IcsCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ProductReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.TestingLabCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.TestingLabReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.VersionReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritanceReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnsupportedCharacterReviewer;

@Component
public class ListingUploadValidator {
    private CSVHeaderReviewer csvHeaderReviewer;
    private ChplNumberFormatReviewer chplNumberFormatReviewer;
    private EditionCodeReviewer editionCodeReviewer;
    private TestingLabCodeReviewer atlCodeReviewer;
    private CertificationBodyCodeReviewer acbCodeReviewer;
    private DeveloperCodeReviewer developerCodeReviewer;
    private IcsCodeReviewer icsCodeReviewer;
    private AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer;
    private CertifiedDateCodeReviewer certifiedDateCodeReviewer;

    private ChplNumberUniqueReviewer chplNumberUniqueReviewer;
    private EditionReviewer editionReviewer;
    private TestingLabReviewer atlReviewer;
    private CertificationBodyReviewer acbReviewer;
    private DeveloperReviewer devReviewer;
    private ProductReviewer productReviewer;
    private VersionReviewer versionReviewer;
    private CertificationDateReviewer certDateReviewer;
    private InheritanceReviewer inheritanceReviewer;
    private UnsupportedCharacterReviewer unsupportedCharacterReviewer;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingUploadValidator(CSVHeaderReviewer csvHeaderReviewer,
            ChplNumberFormatReviewer chplNumberFormatReviewer,
            EditionCodeReviewer editionCodeReviewer,
            TestingLabCodeReviewer atlCodeReviewer,
            CertificationBodyCodeReviewer acbCodeReviewer,
            DeveloperCodeReviewer developerCodeReviewer,
            IcsCodeReviewer icsCodeReviewer,
            AdditionalSoftwareCodeReviewer additionalSoftwareCodeReviewer,
            EditionReviewer editionReviewer,
            @Qualifier("testingLabReviewer") TestingLabReviewer atlReviewer,
            CertificationBodyReviewer acbReviewer,
            DeveloperReviewer devReviewer,
            ProductReviewer productReviewer,
            VersionReviewer versionReviewer,
            CertifiedDateCodeReviewer certifiedDateCodeReviewer,
            CertificationDateReviewer certDateReviewer,
            ChplNumberUniqueReviewer chplNumberUniqueReviewer,
            InheritanceReviewer inheritanceReviewer,
            UnsupportedCharacterReviewer unsupportedCharacterReviewer) {
        this.csvHeaderReviewer = csvHeaderReviewer;
        this.chplNumberFormatReviewer = chplNumberFormatReviewer;
        this.editionCodeReviewer = editionCodeReviewer;
        this.atlCodeReviewer = atlCodeReviewer;
        this.acbCodeReviewer = acbCodeReviewer;
        this.developerCodeReviewer = developerCodeReviewer;
        this.icsCodeReviewer = icsCodeReviewer;
        this.additionalSoftwareCodeReviewer = additionalSoftwareCodeReviewer;
        this.certifiedDateCodeReviewer = certifiedDateCodeReviewer;
        this.editionReviewer = editionReviewer;
        this.atlReviewer = atlReviewer;
        this.acbReviewer = acbReviewer;
        this.devReviewer = devReviewer;
        this.productReviewer = productReviewer;
        this.versionReviewer = versionReviewer;
        this.certDateReviewer = certDateReviewer;
        this.chplNumberUniqueReviewer = chplNumberUniqueReviewer;
        this.inheritanceReviewer = inheritanceReviewer;
        this.unsupportedCharacterReviewer = unsupportedCharacterReviewer;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        csvHeaderReviewer.review(uploadedMetadata, listing);
        chplNumberFormatReviewer.review(listing);
        editionCodeReviewer.review(listing);
        atlCodeReviewer.review(listing);
        acbCodeReviewer.review(listing);
        developerCodeReviewer.review(listing);
        icsCodeReviewer.review(listing);
        additionalSoftwareCodeReviewer.review(listing);
        certifiedDateCodeReviewer.review(listing);
        editionReviewer.review(listing);
        atlReviewer.review(listing);
        acbReviewer.review(listing);
        devReviewer.review(listing);
        productReviewer.review(listing);
        versionReviewer.review(listing);
        certDateReviewer.review(listing);
        chplNumberUniqueReviewer.review(listing);
        inheritanceReviewer.review(listing);
        unsupportedCharacterReviewer.review(listing);
    }
}
