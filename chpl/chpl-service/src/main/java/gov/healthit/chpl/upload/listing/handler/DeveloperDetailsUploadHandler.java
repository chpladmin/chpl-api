package gov.healthit.chpl.upload.listing.handler;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.upload.listing.HeadingPostHti5;
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
                .userEnteredName(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER, headingRecord, listingRecords))
                .userEnteredWebsite(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_WEBSITE, headingRecord, listingRecords))
                .userEnteredSelfDeveloper(parseSelfDeveloperStr(headingRecord, listingRecords))
                .build();
        Address address = Address.builder()
                .line1(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_ADDRESS, headingRecord, listingRecords))
                .line2(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_ADDRESS_LINE_2, headingRecord, listingRecords))
                .city(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_CITY, headingRecord, listingRecords))
                .state(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_STATE, headingRecord, listingRecords))
                .zipcode(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_ZIP, headingRecord, listingRecords))
                .build();
        developer.setUserEnteredAddress(address);
        PointOfContact contact = PointOfContact.builder()
                .fullName(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_CONTACT_NAME, headingRecord, listingRecords))
                .email(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_EMAIL, headingRecord, listingRecords))
                .phoneNumber(uploadUtil.parseSingleRowField(HeadingPostHti5.DEVELOPER_PHONE, headingRecord, listingRecords))
                .build();
        developer.setUserEnteredPointOfContact(contact);
        return developer;
    }

    private String parseSelfDeveloperStr(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(HeadingPostHti5.SELF_DEVELOPER, headingRecord, listingRecords);
    }

    private boolean hasNoDeveloperFields(CSVRecord headingRecord) {
        return !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_WEBSITE, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.SELF_DEVELOPER, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_ADDRESS, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_ADDRESS_LINE_2, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_CITY, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_STATE, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_ZIP, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_CONTACT_NAME, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_EMAIL, headingRecord)
                && !uploadUtil.hasHeading(HeadingPostHti5.DEVELOPER_PHONE, headingRecord);
    }
}
