package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;

@Component("icsUploadHandler")
public class IcsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public IcsUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public InheritedCertificationStatus handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        if (hasNoIcsColumns(headingRecord)) {
            return null;
        }
        InheritedCertificationStatus ics = InheritedCertificationStatus.builder()
                .inherits(parseInherits(headingRecord, listingRecords))
                .inheritsStr(parseInheritsStr(headingRecord, listingRecords))
                .parents(parseParents(headingRecord, listingRecords))
                .build();
        return ics;
    }

    private Boolean parseInherits(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Boolean result = null;
        try {
            result = uploadUtil.parseSingleRowFieldAsBoolean(Heading.ICS, headingRecord, listingRecords);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseInheritsStr(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Heading.ICS, headingRecord, listingRecords);
    }

    private List<CertifiedProduct> parseParents(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Heading.ICS_SOURCE, headingRecord, listingRecords);
        List<CertifiedProduct> cps = new ArrayList<CertifiedProduct>();
        if (values != null) {
            values.stream().forEach(value -> {
                CertifiedProduct cp = CertifiedProduct.builder()
                        .chplProductNumber(value)
                        .build();
                cps.add(cp);
            });
        }
        return cps;
    }

    private boolean hasNoIcsColumns(CSVRecord headingRecord) {
        return !uploadUtil.hasHeading(Heading.ICS, headingRecord)
                && !uploadUtil.hasHeading(Heading.ICS_SOURCE, headingRecord);
    }
}
