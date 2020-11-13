package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("additionalSoftwareUploadHandler")
public class AdditionalSoftwareUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public AdditionalSoftwareUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CertificationResultAdditionalSoftware> handle(CSVRecord headingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        //TODO:
        return additionalSoftware;
    }
}
