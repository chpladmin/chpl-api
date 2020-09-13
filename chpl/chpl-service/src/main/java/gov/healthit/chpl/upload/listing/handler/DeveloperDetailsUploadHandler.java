package gov.healthit.chpl.upload.listing.handler;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("developerDetailsUploadHandler")
@Log4j2
public class DeveloperDetailsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private DeveloperDAO developerDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeveloperDetailsUploadHandler(ListingUploadHandlerUtil uploadUtil,
            DeveloperDAO developerDao, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.developerDao = developerDao;
        this.msgUtil = msgUtil;
    }

    public Developer handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Developer developer = Developer.builder()
                .name(uploadUtil.parseSingleValueField(Headings.DEVELOPER, headingRecord, listingRecords))
                .website(uploadUtil.parseSingleValueField(Headings.DEVELOPER_WEBSITE, headingRecord, listingRecords))
                .selfDeveloper(uploadUtil.parseSingleValueFieldAsBoolean(Headings.SELF_DEVELOPER, headingRecord, listingRecords))
                .build();
        if (!StringUtils.isEmpty(developer.getName())) {
            //TODO: convert query to get a DeveloperEntitySimple
            DeveloperDTO existingDeveloper = developerDao.getByName(developer.getName());
            if (existingDeveloper != null) {
                developer.setDeveloperId(existingDeveloper.getId());
            }
        }
        Address address = Address.builder()
                .line1(uploadUtil.parseSingleValueField(Headings.DEVELOPER_ADDRESS, headingRecord, listingRecords))
                .city(uploadUtil.parseSingleValueField(Headings.DEVELOPER_CITY, headingRecord, listingRecords))
                .state(uploadUtil.parseSingleValueField(Headings.DEVELOPER_STATE, headingRecord, listingRecords))
                .zipcode(uploadUtil.parseSingleValueField(Headings.DEVELOPER_ZIP, headingRecord, listingRecords))
                .build();
        developer.setAddress(address);
        Contact contact = Contact.builder()
                .fullName(uploadUtil.parseSingleValueField(Headings.DEVELOPER_CONTACT_NAME, headingRecord, listingRecords))
                .email(uploadUtil.parseSingleValueField(Headings.DEVELOPER_EMAIL, headingRecord, listingRecords))
                .phoneNumber(uploadUtil.parseSingleValueField(Headings.DEVELOPER_PHONE, headingRecord, listingRecords))
                .build();
        developer.setContact(contact);
        return developer;
    }
}
