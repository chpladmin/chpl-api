package gov.healthit.chpl.scheduler.job.urlStatus.email;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.CertifiedProductSummaryDTO;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "questionableUrlReportGeneratorJobLogger")
public class FailedUrlCsvFormatter {
    private static final int INDEX_URL = 0;
    private static final int INDEX_STATUS_CODE = 1;
    private static final int INDEX_STATUS_NAME = 2;
    private static final int INDEX_ERROR_MESSAGE = 3;
    private static final int INDEX_URL_TYPE = 4;
    private static final int INDEX_CRITERION = 5;
    private static final int INDEX_ATL = 6;
    private static final int INDEX_ACB = 7;
    private static final int INDEX_DEVELOPER_NAME = 8;
    private static final int INDEX_DEVELOPER_CONTACT_NAME = 9;
    private static final int INDEX_DEVELOPER_CONTACT_EMAIL = 10;
    private static final int INDEX_DEVELOPER_CONTACT_PHONE = 11;
    private static final int INDEX_PRODUCT_NAME = 12;
    private static final int INDEX_VERSION = 13;
    private static final int INDEX_CHPL_PRODUCT_NUMBER = 14;
    private static final int INDEX_CERTIFICATION_DATE = 15;
    private static final int INDEX_CERTIFICATION_STATUS = 16;
    private static final int INDEX_CERT_RESULT = 17;
    private static final int INDEX_LAST_CHECKED_DATE = 18;

    private static final String[] CSV_HEADER = {
            "URL", "Status Code", "Status Name", "Error Message", "URL Type", "Certification Criterion",
            "ONC-ATL", "ONC-ACB", "Developer", "Developer Contact Name", "Developer Contact Email",
            "Developer Contact Phone Number", "Product", "Version", "CHPL Product Number",
            "Certification Date", "Certification Status", "Criteria", "Date Last Checked"
    };

    public List<String> getHeaderRow() {
        return Arrays.asList(CSV_HEADER);
    }

    public List<String> getRow(FailedUrlResult urlResult) {
        List<String> row = createRow(urlResult);
        if (urlResult.getCriterion() != null) {
            row.set(INDEX_CRITERION, Util.formatCriteriaNumber(urlResult.getCriterion()));
        }
        if (urlResult.getAcb() != null) {
            row.set(INDEX_ACB, urlResult.getAcb().getName());
        }
        if (urlResult.getAtl() != null) {
            row.set(INDEX_ATL, urlResult.getAtl().getName());
        }
        Developer developer = urlResult.getDeveloper();
        if (developer != null) {
            row.set(INDEX_DEVELOPER_NAME, developer.getName());
            if (developer.getContact() != null) {
                row.set(INDEX_DEVELOPER_CONTACT_NAME, developer.getContact().getFullName());
                row.set(INDEX_DEVELOPER_CONTACT_EMAIL, developer.getContact().getEmail());
                row.set(INDEX_DEVELOPER_CONTACT_PHONE, developer.getContact().getPhoneNumber());
            }
        }
        CertifiedProductSummaryDTO listing = urlResult.getListing();
        if (listing != null && listing.getProduct() != null) {
            row.set(INDEX_PRODUCT_NAME, listing.getProduct().getName());
        }
        if (listing != null && listing.getVersion() != null) {
            row.set(INDEX_VERSION, listing.getVersion().getVersion());
        }
        if (listing != null) {
            row.set(INDEX_CHPL_PRODUCT_NUMBER, listing.getChplProductNumber());
            row.set(INDEX_CERTIFICATION_DATE, getDateFormatter().format(listing.getCertificationDate()));
            row.set(INDEX_CERTIFICATION_STATUS, listing.getCertificationStatus());
        }
        if (urlResult.getCertResult() != null) {
            row.set(INDEX_CERT_RESULT, urlResult.getCertResult().getCriterion().getNumber());
        }
        return row;
    }

    private List<String> createRow(FailedUrlResult urlResult) {
        List<String> result = new ArrayList<String>(CSV_HEADER.length);
        for (int i = 0; i < CSV_HEADER.length; i++) {
            result.add("");
        }
        result.set(INDEX_URL, urlResult.getUrl());
        if (urlResult.getResponseCode() != null) {
            result.set(INDEX_STATUS_CODE, urlResult.getResponseCode().toString());
            try {
                HttpStatus httpStatus = HttpStatus.valueOf(urlResult.getResponseCode());
                if (httpStatus != null) {
                    result.set(INDEX_STATUS_NAME, httpStatus.getReasonPhrase());
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("No HttpStatus object could be found for response code " + urlResult.getResponseCode());
            }
        }

        if (urlResult.getResponseMessage() != null) {
            result.set(INDEX_ERROR_MESSAGE, urlResult.getResponseMessage());
        }
        result.set(INDEX_URL_TYPE, urlResult.getUrlType().getName());
        result.set(INDEX_LAST_CHECKED_DATE, getTimestampFormatter().format(urlResult.getLastChecked()));
        return result;
    }

    private DateFormat getDateFormatter() {
        return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    }

    private DateFormat getTimestampFormatter() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US);
    }
}
