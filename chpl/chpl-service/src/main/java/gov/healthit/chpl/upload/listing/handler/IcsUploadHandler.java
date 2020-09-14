package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("icsUploadHandler")
@Log4j2
public class IcsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public IcsUploadHandler(ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public InheritedCertificationStatus handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        if (hasNoIcsColumns(headingRecord)) {
            return null;
        }
        InheritedCertificationStatus ics = InheritedCertificationStatus.builder()
                .inherits(uploadUtil.parseSingleRowFieldAsBoolean(Headings.ICS, headingRecord, listingRecords))
                .parents(parseParents(headingRecord, listingRecords))
                .build();
        return ics;
    }

    private List<CertifiedProduct> parseParents(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Headings.ICS_SOURCE, headingRecord, listingRecords);
        List<CertifiedProduct> cps = new ArrayList<CertifiedProduct>();
        values.stream().forEach(value -> {
            CertifiedProduct cp = CertifiedProduct.builder()
                    .chplProductNumber(value)
                    .build();
            cps.add(cp);
        });
        return cps;
    }

    private boolean hasNoIcsColumns(CSVRecord headingRecord) {
        return !uploadUtil.hasHeading(Headings.ICS, headingRecord)
                && !uploadUtil.hasHeading(Headings.ICS_SOURCE, headingRecord);
    }
}
