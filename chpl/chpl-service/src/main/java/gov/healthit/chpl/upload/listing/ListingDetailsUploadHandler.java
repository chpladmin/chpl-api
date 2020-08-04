package gov.healthit.chpl.upload.listing;

import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
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

    public ListingUpload parseAsListing(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        ListingUpload result = new ListingUpload();

        String chplId = parseChplId(result, headingRecord, listingRecords);
        Boolean accessibilityCertified = parseAccessibilityCertified(result, headingRecord, listingRecords);
        Date certificationDate = parseCertificationDate(result, headingRecord, listingRecords);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(chplId)
                .acbCertificationId(uploadUtil.parseSingleValueField(
                        Headings.ACB_CERTIFICATION_ID, headingRecord, listingRecords))
                .accessibilityCertified(accessibilityCertified)
                .certificationDate(certificationDate != null ? certificationDate.getTime() : null)
            .build();

        result.setListing(listing);
        return result;
    }

    private String parseChplId(ListingUpload uploadMetadata, CSVRecord headingRecord,
            List<CSVRecord> listingRecords) {
        String chplId = null;
        try {
            chplId = uploadUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecord, listingRecords);
        } catch (Exception ex) {
            uploadMetadata.getUploadErrors().add(ex.getMessage());
        }
        return chplId;
    }

    private Boolean parseAccessibilityCertified(ListingUpload uploadMetadata, CSVRecord headingRecord,
            List<CSVRecord> listingRecords) {
        Boolean accessibilityCertified = null;
        try {
            accessibilityCertified = uploadUtil.parseSingleValueFieldAsBoolean(
                Headings.ACCESSIBILITY_CERTIFIED, headingRecord, listingRecords);
        } catch (Exception ex) {
            uploadMetadata.getUploadErrors().add(ex.getMessage());
        }
        return accessibilityCertified;
    }

    private Date parseCertificationDate(ListingUpload uploadMetadata, CSVRecord headingRecord,
            List<CSVRecord> listingRecords) {
        Date certificationDate = null;
        try {
            certificationDate = uploadUtil.parseSingleValueFieldAsDate(
                Headings.CERTIFICATION_DATE, headingRecord, listingRecords);
        } catch (Exception ex) {
            uploadMetadata.getUploadErrors().add(ex.getMessage());
        }
        return certificationDate;
    }
}
