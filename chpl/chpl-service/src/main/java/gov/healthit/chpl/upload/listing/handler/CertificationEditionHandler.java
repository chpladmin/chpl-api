package gov.healthit.chpl.upload.listing.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("certificationEditionUploadHandler")
public class CertificationEditionHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public CertificationEditionHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public CertificationEdition handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String certificationYear = readCertificationYearFromFile(headingRecord, listingRecords);
        if (StringUtils.isEmpty(certificationYear)) {
            return null;
        }
        return CertificationEdition.builder()
                .id(null)
                .certificationEditionId(null)
                .name(certificationYear)
                .year(certificationYear)
                .build();
    }

    @Deprecated
    public Map<String, Object> handleDeprecated(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String certificationYear = readCertificationYearFromFile(headingRecord, listingRecords);
        if (StringUtils.isEmpty(certificationYear)) {
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
}
