package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnouncementActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("announcementActivityMetadataBuilder")
public class AnnouncementActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Autowired
    public AnnouncementActivityMetadataBuilder(CognitoUserService cognitoUserService, UserDAO userDAO) {
        super(cognitoUserService, userDAO);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        if (!(metadata instanceof AnnouncementActivityMetadata)) {
            return;
        }
    }

}
