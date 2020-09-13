package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("targetedUsersUploadHandler")
@Log4j2
public class TargetedUsersUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private TargetedUserDAO dao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TargetedUsersUploadHandler(ListingUploadHandlerUtil uploadUtil,
            TargetedUserDAO dao, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.dao = dao;
        this.msgUtil = msgUtil;
    }

    public List<CertifiedProductTargetedUser> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();
        List<String> targetedUserNames = parseTargetedUserNames(headingRecord, listingRecords);
        if (targetedUserNames != null && targetedUserNames.size() > 0) {
            targetedUserNames.stream().forEach(name -> {
                try {
                    TargetedUserDTO targetedUser = dao.getByName(name);
                    if (targetedUser != null) {
                        targetedUsers.add(CertifiedProductTargetedUser.builder()
                                .targetedUserId(targetedUser.getId())
                                .targetedUserName(targetedUser.getName())
                                .build());
                    } else {
                        LOGGER.warn("Targeted user with name '" + name + "' was not found.");
                        targetedUsers.add(CertifiedProductTargetedUser.builder()
                                .targetedUserName(name)
                                .build());
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception looking up targeted user '" + name + "'." + e.getMessage());
                }
            });
        }
        return targetedUsers;
    }

    private List<String> parseTargetedUserNames(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiValueField(
                Headings.TARGETED_USERS, headingRecord, listingRecords);
        return values;
    }
}
