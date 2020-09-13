package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("accessibilityStandardsUploadHandler")
@Log4j2
public class AccessibilityStandardsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private AccessibilityStandardDAO dao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AccessibilityStandardsUploadHandler(ListingUploadHandlerUtil uploadUtil,
            AccessibilityStandardDAO dao, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.dao = dao;
        this.msgUtil = msgUtil;
    }

    public List<CertifiedProductAccessibilityStandard> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductAccessibilityStandard> accStds = new ArrayList<CertifiedProductAccessibilityStandard>();
        List<String> accStdNames = parseAccessibilityStandardNames(headingRecord, listingRecords);
        if (accStdNames != null && accStdNames.size() > 0) {
            accStdNames.stream().forEach(name -> {
                try {
                    AccessibilityStandardDTO accStd = dao.getByName(name);
                    if (accStd != null) {
                        accStds.add(CertifiedProductAccessibilityStandard.builder()
                                .accessibilityStandardId(accStd.getId())
                                .accessibilityStandardName(accStd.getName())
                                .build());
                    } else {
                        LOGGER.warn("Accessibility standard with name '" + name + "' was not found.");
                        accStds.add(CertifiedProductAccessibilityStandard.builder()
                                .accessibilityStandardName(name)
                                .build());
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception looking up accessibility standard '" + name + "'." + e.getMessage());
                }
            });
        }
        return accStds;
    }

    private List<String> parseAccessibilityStandardNames(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiValueField(
                Headings.ACCESSIBILITY_STANDARD, headingRecord, listingRecords);
        return values;
    }
}
