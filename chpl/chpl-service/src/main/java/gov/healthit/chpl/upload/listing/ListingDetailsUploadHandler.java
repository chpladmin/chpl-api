package gov.healthit.chpl.upload.listing;

import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingDetailsUploadHandler")
@Log4j2
public class ListingDetailsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingDetailsUploadHandler(ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public CertifiedProductSearchDetails parseAsListing(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Date certificationDate = uploadUtil.parseSingleValueFieldAsDate(
                Headings.CERTIFICATION_DATE, headingRecord, listingRecords);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(uploadUtil.parseSingleValueField(Headings.UNIQUE_ID, headingRecord, listingRecords))
                .acbCertificationId(uploadUtil.parseSingleValueField(
                        Headings.ACB_CERTIFICATION_ID, headingRecord, listingRecords))
                .accessibilityCertified(uploadUtil.parseSingleValueFieldAsBoolean(
                        Headings.ACCESSIBILITY_CERTIFIED, headingRecord, listingRecords))
                .certificationDate(certificationDate != null ? certificationDate.getTime() : null)
            .build();

        return listing;
    }
}
