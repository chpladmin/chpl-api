package gov.healthit.chpl.upload.listing.handler;

import java.util.Date;
import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingDetailsUploadHandler")
@Log4j2
public class ListingDetailsUploadHandler {
    private AccessibilityStandardsUploadHandler accessibilityStandardsHandler;
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingDetailsUploadHandler(AccessibilityStandardsUploadHandler accessibilityStandardsHandler,
            ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.accessibilityStandardsHandler = accessibilityStandardsHandler;
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public CertifiedProductSearchDetails parseAsListing(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        String chplId = parseChplId(headingRecord, listingRecords);
        Boolean accessibilityCertified = parseAccessibilityCertified(headingRecord, listingRecords);
        Date certificationDate = parseCertificationDate(headingRecord, listingRecords);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(chplId)
                .acbCertificationId(uploadUtil.parseSingleValueField(
                        Headings.ACB_CERTIFICATION_ID, headingRecord, listingRecords))
                .accessibilityCertified(accessibilityCertified)
                .certificationDate(certificationDate != null ? certificationDate.getTime() : null)
                .accessibilityStandards(accessibilityStandardsHandler.handle(headingRecord, listingRecords))
            .build();

        return listing;
    }

    private String parseChplId(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String chplId = uploadUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecord, listingRecords);
        return chplId;
    }

    private Boolean parseAccessibilityCertified(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Boolean accessibilityCertified = uploadUtil.parseSingleValueFieldAsBoolean(
                Headings.ACCESSIBILITY_CERTIFIED, headingRecord, listingRecords);
        return accessibilityCertified;
    }

    private Date parseCertificationDate(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Date certificationDate = uploadUtil.parseSingleValueFieldAsDate(
                Headings.CERTIFICATION_DATE, headingRecord, listingRecords);
        return certificationDate;
    }
}
