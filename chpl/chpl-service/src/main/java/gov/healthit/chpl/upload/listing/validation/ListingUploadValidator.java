package gov.healthit.chpl.upload.listing.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.validation.reviewer.AccessibilityStandardReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.AdditionalSoftwareCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CSVHeaderReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertificationBodyCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertificationBodyReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertificationResultReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CertifiedDateCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ChplNumberFormatReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ChplNumberUniqueReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.CqmResultReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.DeveloperCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.DeveloperReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.EditionCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.EditionReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.IcsCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.ProductReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.QmsStandardReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.SedReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.TestingLabCodeReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.TestingLabReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.VersionReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DuplicateDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritanceReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UrlReviewer;

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
    private QmsStandardReviewer qmsReviewer;
    private AccessibilityStandardReviewer accStdReviewer;
    private DuplicateDataReviewer duplicateDataReviewer;
    private UrlReviewer urlReviewer;
    private FieldLengthReviewer fieldLengthReviewer;
    private UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    private CertificationResultReviewer certResultReviewer;
    private CqmResultReviewer cqmResultReviewer;
    private SedReviewer sedReviewer;

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
            QmsStandardReviewer qmsReviewer,
            AccessibilityStandardReviewer accStdReviewer,
            DuplicateDataReviewer duplicateDataReviewer,
            UrlReviewer urlReviewer,
            FieldLengthReviewer fieldLengthReviewer,
            UnsupportedCharacterReviewer unsupportedCharacterReviewer,
            CertificationResultReviewer certResultReviewer,
            CqmResultReviewer cqmResultReviewer,
            SedReviewer sedReviewer) {
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
        this.qmsReviewer = qmsReviewer;
        this.accStdReviewer = accStdReviewer;
        this.duplicateDataReviewer = duplicateDataReviewer;
        this.urlReviewer = urlReviewer;
        this.fieldLengthReviewer = fieldLengthReviewer;
        this.unsupportedCharacterReviewer = unsupportedCharacterReviewer;
        this.certResultReviewer = certResultReviewer;
        this.cqmResultReviewer = cqmResultReviewer;
        this.sedReviewer = sedReviewer;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        csvHeaderReviewer.review(uploadedMetadata, listing);
        chplNumberFormatReviewer.review(listing);
        chplNumberUniqueReviewer.review(listing);
        editionCodeReviewer.review(listing);
        editionReviewer.review(listing);
        atlCodeReviewer.review(listing);
        atlReviewer.review(listing);
        acbCodeReviewer.review(listing);
        acbReviewer.review(listing);
        developerCodeReviewer.review(listing);
        devReviewer.review(listing);
        productReviewer.review(listing);
        versionReviewer.review(listing);
        icsCodeReviewer.review(listing);
        inheritanceReviewer.review(listing);
        additionalSoftwareCodeReviewer.review(listing);
        certifiedDateCodeReviewer.review(listing);
        certDateReviewer.review(listing);
        qmsReviewer.review(listing);
        accStdReviewer.review(listing);
        urlReviewer.review(listing);
        fieldLengthReviewer.review(listing);
        unsupportedCharacterReviewer.review(listing);
        certResultReviewer.review(listing);
        cqmResultReviewer.review(listing);
        sedReviewer.review(listing);
        duplicateDataReviewer.review(listing);
    }
}
