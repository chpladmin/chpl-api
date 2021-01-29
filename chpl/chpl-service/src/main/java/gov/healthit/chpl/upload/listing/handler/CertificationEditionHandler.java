package gov.healthit.chpl.upload.listing.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Component("certificationEditionUploadHandler")
@Log4j2
public class CertificationEditionHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public CertificationEditionHandler(ListingUploadHandlerUtil uploadUtil,
            ChplProductNumberUtil chplProductNumberUtil) {
        this.uploadUtil = uploadUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    public Map<String, Object> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String certificationYear = readCertificationYearFromFile(headingRecord, listingRecords);
        if (StringUtils.isEmpty(certificationYear)) {
            certificationYear = parseCertificationYearFromChplProductNumber(headingRecord, listingRecords);
        }
        if (certificationYear == null) {
            return null;
        }
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, certificationYear);
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, null);
       return edition;
    }

    private String readCertificationYearFromFile(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.EDITION, headingRecord, listingRecords);
    }

    private String parseCertificationYearFromChplProductNumber(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String certificationYear = null;
        try {
            String chplProductNumber = uploadUtil.parseRequiredSingleRowField(Headings.UNIQUE_ID, headingRecord, listingRecords);
            if (!StringUtils.isEmpty(chplProductNumber)) {
                String editionCode = chplProductNumberUtil.getCertificationEditionCode(chplProductNumber);
                if (!StringUtils.isEmpty(editionCode)) {
                    if (editionCode.length() == 1) {
                        editionCode = "0" + editionCode;
                    }
                    if (editionCode.length() == 2) {
                        certificationYear = "20" + editionCode;
                    }
                } else {
                    LOGGER.warn("Certification edition code in the chpl product number " + chplProductNumber + " was empty.");
                }
            } else {
                LOGGER.warn("No chpl product number was found in the file.");
            }
        } catch (ValidationException | ArrayIndexOutOfBoundsException ex) { }
        return certificationYear;
    }
}
