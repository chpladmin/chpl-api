package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.auth.CognitoUserService;

@Component("pendingSurveillanceActivityMetadataBuilder")
public class PendingSurveillanceActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Autowired
    public PendingSurveillanceActivityMetadataBuilder(CognitoUserService cognitoUserService, UserDAO userDAO) {
        super(cognitoUserService, userDAO);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {

    }
}
