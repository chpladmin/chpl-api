package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("qmsUploadHandler")
@Log4j2
public class QmsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private QmsStandardDAO dao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public QmsUploadHandler(ListingUploadHandlerUtil uploadUtil,
            QmsStandardDAO dao, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.dao = dao;
        this.msgUtil = msgUtil;
    }

    public List<CertifiedProductQmsStandard> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();
        List<String> qmsStandardNames = parseQmsStandardNames(headingRecord, listingRecords);
        List<String> qmsApplicableCriteria = parseQmsApplicableCriteria(headingRecord, listingRecords);
        List<String> qmsModifications = parseQmsModifications(headingRecord, listingRecords);
        //TODO: how to make sure the lists line up? maybe not all rows/cols have data?
        return qmsStandards;
    }

    private List<String> parseQmsStandardNames(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiValueField(
                Headings.QMS_STANDARD_NAME, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseQmsApplicableCriteria(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiValueField(
                Headings.QMS_STANDARD_APPLICABLE_CRITERIA, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseQmsModifications(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiValueField(
                Headings.QMS_MODIFICATION, headingRecord, listingRecords);
        return values;
    }
}
