package gov.healthit.chpl.upload.listing.validation;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component
public class CSVHeaderReviewer {
    private ListingUploadHandlerUtil handlerUtil;

    @Autowired
    public CSVHeaderReviewer(ListingUploadHandlerUtil handlerUtil) {
        this.handlerUtil = handlerUtil;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        if (uploadedMetadata.getRecords() == null && uploadedMetadata.getRecords().size() == 0) {
            return;
        }
        CSVRecord heading = handlerUtil.getHeadingRecord(uploadedMetadata.getRecords());
        //TODO: check if any headings don't match expected
    }
}
