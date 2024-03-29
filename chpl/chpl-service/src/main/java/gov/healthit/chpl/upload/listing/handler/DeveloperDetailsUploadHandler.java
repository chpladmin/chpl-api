package gov.healthit.chpl.upload.listing.handler;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("developerDetailsUploadHandler")
public class DeveloperDetailsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public DeveloperDetailsUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public Developer handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        if (hasNoDeveloperFields(headingRecord)) {
            return null;
        }

        Developer developer = Developer.builder()
                .userEnteredName(uploadUtil.parseSingleRowField(Headings.DEVELOPER, headingRecord, listingRecords))
                .userEnteredWebsite(uploadUtil.parseSingleRowField(Headings.DEVELOPER_WEBSITE, headingRecord, listingRecords))
                .userEnteredSelfDeveloper(parseSelfDeveloperStr(headingRecord, listingRecords))
                .build();
        Address address = Address.builder()
                .line1(uploadUtil.parseSingleRowField(Headings.DEVELOPER_ADDRESS, headingRecord, listingRecords))
                .line2(uploadUtil.parseSingleRowField(Headings.DEVELOPER_ADDRESS_LINE_2, headingRecord, listingRecords))
                .city(uploadUtil.parseSingleRowField(Headings.DEVELOPER_CITY, headingRecord, listingRecords))
                .state(uploadUtil.parseSingleRowField(Headings.DEVELOPER_STATE, headingRecord, listingRecords))
                .zipcode(uploadUtil.parseSingleRowField(Headings.DEVELOPER_ZIP, headingRecord, listingRecords))
                .build();
        developer.setUserEnteredAddress(address);
        PointOfContact contact = PointOfContact.builder()
                .fullName(uploadUtil.parseSingleRowField(Headings.DEVELOPER_CONTACT_NAME, headingRecord, listingRecords))
                .email(uploadUtil.parseSingleRowField(Headings.DEVELOPER_EMAIL, headingRecord, listingRecords))
                .phoneNumber(uploadUtil.parseSingleRowField(Headings.DEVELOPER_PHONE, headingRecord, listingRecords))
                .build();
        developer.setUserEnteredPointOfContact(contact);
        return developer;
    }

    private String parseSelfDeveloperStr(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.SELF_DEVELOPER, headingRecord, listingRecords);
    }

    private boolean hasNoDeveloperFields(CSVRecord headingRecord) {
        return !uploadUtil.hasHeading(Headings.DEVELOPER, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_WEBSITE, headingRecord)
                && !uploadUtil.hasHeading(Headings.SELF_DEVELOPER, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_ADDRESS, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_ADDRESS_LINE_2, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_CITY, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_STATE, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_ZIP, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_CONTACT_NAME, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_EMAIL, headingRecord)
                && !uploadUtil.hasHeading(Headings.DEVELOPER_PHONE, headingRecord);
    }
}
