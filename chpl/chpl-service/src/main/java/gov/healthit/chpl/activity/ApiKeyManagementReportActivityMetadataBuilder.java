package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.auth.CognitoUserService;

@Component("apiKeyManagementReportActivityMetadataBuilder")
public class ApiKeyManagementReportActivityMetadataBuilder extends ActivityMetadataBuilder{

    @Autowired
    public ApiKeyManagementReportActivityMetadataBuilder(CognitoUserService cognitoUserService, UserDAO userDAO) {
        super(cognitoUserService, userDAO);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {

    }
}
