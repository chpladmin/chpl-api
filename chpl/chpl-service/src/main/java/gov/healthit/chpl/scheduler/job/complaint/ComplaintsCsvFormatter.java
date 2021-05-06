package gov.healthit.chpl.scheduler.job.complaint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import gov.healthit.chpl.domain.complaint.Complaint;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "complaintsReportJobLogger")
public class ComplaintsCsvFormatter {
    private static final int INDEX_COMPLAINT_ID = 0;
    private static final int INDEX_ONC_ACB = 1;
    private static final int INDEX_COMPLAINANT_TYPE_NAME = 2;
    private static final int INDEX_COMPLAINANT_TYPE_OTHER = 3;
    private static final int INDEX_ONC_COMPLAINT_ID = 4;
    private static final int INDEX_ACB_COMPLAINT_ID = 5;
    private static final int INDEX_RECEIVED_DATE = 6;
    private static final int INDEX_SUMMARY = 7;
    private static final int INDEX_ACTIONS = 8;
    private static final int INDEX_COMPLAINANT_CONTACTED = 9;
    private static final int INDEX_DEVELOPER_CONTACTED = 10;
    private static final int INDEX_ATL_CONTACTED = 11;
    private static final int INDEX_INFORMED_ONC_PER_170523S = 12;
    private static final int INDEX_CLOSED_DATE = 13;
    private static final int INDEX_ASSOCIATED_LISTINGS = 14;
    private static final int INDEX_DEVELOPER_NAME = 15;
    private static final int INDEX_PRODUCT_NAME = 16;
    private static final int INDEX_VERSION_NAME = 17;
    private static final int INDEX_ASSOCIATED_SURVEILLANCE = 18;
    private static final int INDEX_ASSOCIATED_CRITERIA = 19;

    private static final String[] CSV_HEADER = {
            "Complaint ID", "ONC-ACB", "Complainant Type Name", "Complainant Type Other", "ONC Complaint ID",
            "ONC-ACB Complaint ID", "Received Date", "Summary", "Actions", "Complainant Contacted",
            "Developer Contacted", "ONC-ATL Contacted", "Informed ONC per 170.523 (s)", "Closed Date",
            "Associated Listings", "Developer", "Product", "Version", "Associated Surveillance",
            "Associated Criteria"
    };

    public List<String> getHeaderRow() {
        return Arrays.asList(CSV_HEADER);
    }

    public List<String> getRow(Complaint complaint) {
        List<String> row = createRow(complaint);
//        if (urlResult.getAcb() != null) {
//            row.set(INDEX_ACB, urlResult.getAcb().getName());
//        }
//        if (urlResult.getAtl() != null) {
//            row.set(INDEX_ATL, urlResult.getAtl().getName());
//        }
//        Developer developer = urlResult.getDeveloper();
//        if (developer != null) {
//            row.set(INDEX_DEVELOPER_NAME, developer.getName());
//            if (developer.getContact() != null) {
//                row.set(INDEX_DEVELOPER_CONTACT_NAME, developer.getContact().getFullName());
//                row.set(INDEX_DEVELOPER_CONTACT_EMAIL, developer.getContact().getEmail());
//                row.set(INDEX_DEVELOPER_CONTACT_PHONE, developer.getContact().getPhoneNumber());
//            }
//        }
//        CertifiedProductSummaryDTO listing = urlResult.getListing();
//        if (listing != null && listing.getProduct() != null) {
//            row.set(INDEX_PRODUCT_NAME, listing.getProduct().getName());
//        }
//        if (listing != null && listing.getVersion() != null) {
//            row.set(INDEX_VERSION, listing.getVersion().getVersion());
//        }
//        if (listing != null) {
//            row.set(INDEX_CHPL_PRODUCT_NUMBER, listing.getChplProductNumber());
//            row.set(INDEX_EDITION, listing.getYear());
//            row.set(INDEX_CERTIFICATION_DATE, getDateFormatter().format(listing.getCertificationDate()));
//            row.set(INDEX_CERTIFICATION_STATUS, listing.getCertificationStatus());
//        }
//        if (urlResult.getCertResult() != null) {
//            row.set(INDEX_CRITERION, urlResult.getCertResult().getCriterion().getNumber());
//        }
        return row;
    }

    private List<String> createRow(Complaint complaint) {
        List<String> result = new ArrayList<String>(CSV_HEADER.length);
        for (int i = 0; i < CSV_HEADER.length; i++) {
            result.add("");
        }
//        result.set(INDEX_URL, urlResult.getUrl());
//        if (urlResult.getResponseCode() != null) {
//            result.set(INDEX_STATUS_CODE, urlResult.getResponseCode().toString());
//            try {
//                HttpStatus httpStatus = HttpStatus.valueOf(urlResult.getResponseCode());
//                if (httpStatus != null) {
//                    result.set(INDEX_STATUS_NAME, httpStatus.getReasonPhrase());
//                }
//            } catch (IllegalArgumentException ex) {
//                LOGGER.warn("No HttpStatus object could be found for response code " + urlResult.getResponseCode());
//            }
//        }
//
//        if (urlResult.getResponseMessage() != null) {
//            result.set(INDEX_ERROR_MESSAGE, urlResult.getResponseMessage());
//        }
//        result.set(INDEX_URL_TYPE, urlResult.getUrlType().getName());
//        result.set(INDEX_LAST_CHECKED_DATE, getTimestampFormatter().format(urlResult.getLastChecked()));
        return result;
    }

    private DateFormat getDateFormatter() {
        return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    }

    private DateFormat getTimestampFormatter() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US);
    }
}
