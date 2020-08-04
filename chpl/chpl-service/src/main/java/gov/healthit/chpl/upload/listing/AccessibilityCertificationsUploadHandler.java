package gov.healthit.chpl.upload.listing;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("accessibilityCertificationsUploadHandler")
@Log4j2
public class AccessibilityCertificationsUploadHandler {
    private AccessibilityStandardDAO dao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AccessibilityCertificationsUploadHandler(AccessibilityStandardDAO dao, ErrorMessageUtil msgUtil) {
        this.dao = dao;
        this.msgUtil = msgUtil;
    }

    public List<CertifiedProductAccessibilityStandard> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {

        return null;
    }
}
