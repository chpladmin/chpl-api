package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CSVHeaderReviewer {
    private ListingUploadHandlerUtil handlerUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CSVHeaderReviewer(ListingUploadHandlerUtil handlerUtil, ErrorMessageUtil msgUtil) {
        this.handlerUtil = handlerUtil;
        this.msgUtil = msgUtil;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        if (uploadedMetadata.getRecords() == null || uploadedMetadata.getRecords().size() == 0) {
            return;
        }
        CSVRecord heading = handlerUtil.getHeadingRecord(uploadedMetadata.getRecords());
        heading.forEach(headingVal -> {
            if (!StringUtils.isEmpty(headingVal) && Headings.getHeading(headingVal) == null) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.upload.unrecognizedHeading", headingVal));
            }
        });
    }
}
