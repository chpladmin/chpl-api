package gov.healthit.chpl.upload.listing.handler;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;

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
                .userEnteredName(uploadUtil.parseSingleRowField(Heading.DEVELOPER, headingRecord, listingRecords))
                .userEnteredWebsite(uploadUtil.parseSingleRowField(Heading.DEVELOPER_WEBSITE, headingRecord, listingRecords))
                .userEnteredSelfDeveloper(parseSelfDeveloperStr(headingRecord, listingRecords))
                .build();
        Address address = Address.builder()
                .line1(uploadUtil.parseSingleRowField(Heading.DEVELOPER_ADDRESS, headingRecord, listingRecords))
                .line2(uploadUtil.parseSingleRowField(Heading.DEVELOPER_ADDRESS_LINE_2, headingRecord, listingRecords))
                .city(uploadUtil.parseSingleRowField(Heading.DEVELOPER_CITY, headingRecord, listingRecords))
                .state(uploadUtil.parseSingleRowField(Heading.DEVELOPER_STATE, headingRecord, listingRecords))
                .zipcode(uploadUtil.parseSingleRowField(Heading.DEVELOPER_ZIP, headingRecord, listingRecords))
                .build();
        developer.setUserEnteredAddress(address);
        PointOfContact contact = PointOfContact.builder()
                .fullName(uploadUtil.parseSingleRowField(Heading.DEVELOPER_CONTACT_NAME, headingRecord, listingRecords))
                .email(uploadUtil.parseSingleRowField(Heading.DEVELOPER_EMAIL, headingRecord, listingRecords))
                .phoneNumber(uploadUtil.parseSingleRowField(Heading.DEVELOPER_PHONE, headingRecord, listingRecords))
                .build();
        developer.setUserEnteredPointOfContact(contact);
        return developer;
    }

    private String parseSelfDeveloperStr(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Heading.SELF_DEVELOPER, headingRecord, listingRecords);
    }

    private boolean hasNoDeveloperFields(CSVRecord headingRecord) {
        return !uploadUtil.hasHeading(Heading.DEVELOPER, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_WEBSITE, headingRecord)
                && !uploadUtil.hasHeading(Heading.SELF_DEVELOPER, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_ADDRESS, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_ADDRESS_LINE_2, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_CITY, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_STATE, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_ZIP, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_CONTACT_NAME, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_EMAIL, headingRecord)
                && !uploadUtil.hasHeading(Heading.DEVELOPER_PHONE, headingRecord);
    }
}
