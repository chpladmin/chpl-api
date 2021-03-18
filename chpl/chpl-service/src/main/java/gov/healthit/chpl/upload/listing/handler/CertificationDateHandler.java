package gov.healthit.chpl.upload.listing.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Component("certificationDateUploadHandler")
@Log4j2
public class CertificationDateHandler {
    private static final String CERT_DATE_CODE = "yyMMdd";
    private DateFormat dateFormat;

    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public CertificationDateHandler(ListingUploadHandlerUtil uploadUtil,
            ChplProductNumberUtil chplProductNumberUtil) {
        this.uploadUtil = uploadUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.dateFormat = new SimpleDateFormat(CERT_DATE_CODE);
    }

    public LocalDate handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        LocalDate certificationDate = readCertificationDateFromFile(headingRecord, listingRecords);
        if (certificationDate == null) {
            certificationDate = parseCertificationDateFromChplProductNumber(headingRecord, listingRecords);
        }
       return certificationDate;
    }

    private LocalDate readCertificationDateFromFile(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        LocalDate certificationDate = null;
        try {
            Date certDateFromFile = uploadUtil.parseSingleRowFieldAsDate(Headings.CERTIFICATION_DATE, headingRecord, listingRecords);
            if (certDateFromFile != null) {
                certificationDate = certDateFromFile.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (ValidationException ex) { }
        return certificationDate;
    }

    private LocalDate parseCertificationDateFromChplProductNumber(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        LocalDate certificationDate = null;
        try {
            String chplProductNumber = uploadUtil.parseRequiredSingleRowField(Headings.UNIQUE_ID, headingRecord, listingRecords);
            if (!StringUtils.isEmpty(chplProductNumber)) {
                String certDateCode = chplProductNumberUtil.getCertificationDateCode(chplProductNumber);
                if (!StringUtils.isEmpty(certDateCode) && certDateCode.matches("[0-9]{6}")) {
                    try {
                        Date certDateFromChplNumber = dateFormat.parse(certDateCode);
                        certificationDate = certDateFromChplNumber.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    } catch (ParseException ex) {
                        LOGGER.error("Could not determine certification date from string " + certDateCode, ex);
                    }
                } else {
                    LOGGER.warn("Certification date code in the chpl product number " + chplProductNumber + " was not in the yyMMdd format.");
                }
            } else {
                LOGGER.warn("No chpl product number was found in the file.");
            }
        } catch (ValidationException | ArrayIndexOutOfBoundsException ex) { }
        return certificationDate;
    }
}
