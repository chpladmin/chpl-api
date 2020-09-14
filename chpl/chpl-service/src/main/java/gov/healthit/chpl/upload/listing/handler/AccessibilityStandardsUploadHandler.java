package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("accessibilityStandardsUploadHandler")
@Log4j2
public class AccessibilityStandardsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AccessibilityStandardsUploadHandler(ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public List<CertifiedProductAccessibilityStandard> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductAccessibilityStandard> accStds = new ArrayList<CertifiedProductAccessibilityStandard>();
        List<String> accStdNames = parseAccessibilityStandardNames(headingRecord, listingRecords);
        if (accStdNames != null && accStdNames.size() > 0) {
            accStdNames.stream().forEach(name -> {
                accStds.add(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName(name)
                        .build());
            });
        }
        return accStds;
    }

    private List<String> parseAccessibilityStandardNames(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Headings.ACCESSIBILITY_STANDARD, headingRecord, listingRecords);
        return values;
    }
}
