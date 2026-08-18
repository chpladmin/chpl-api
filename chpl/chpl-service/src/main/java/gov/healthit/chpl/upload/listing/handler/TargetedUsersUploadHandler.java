package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.targeteduser.CertifiedProductTargetedUser;
import gov.healthit.chpl.upload.listing.HeadingPostHti5;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("targetedUsersUploadHandler")
public class TargetedUsersUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public TargetedUsersUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CertifiedProductTargetedUser> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();
        List<String> targetedUserNames = parseTargetedUserNames(headingRecord, listingRecords);
        if (targetedUserNames != null && targetedUserNames.size() > 0) {
            targetedUserNames.stream().forEach(name -> {
                targetedUsers.add(CertifiedProductTargetedUser.builder()
                        .targetedUserName(name)
                        .build());
            });
        }
        return targetedUsers;
    }

    private List<String> parseTargetedUserNames(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                HeadingPostHti5.TARGETED_USERS, headingRecord, listingRecords);
        return values;
    }
}
