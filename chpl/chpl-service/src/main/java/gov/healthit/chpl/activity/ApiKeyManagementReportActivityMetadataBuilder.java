package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;

@Component("apiKeyManagementReportActivityMetadataBuilder")
public class ApiKeyManagementReportActivityMetadataBuilder extends ActivityMetadataBuilder{

    @Autowired
    public ApiKeyManagementReportActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil) {
        super(chplUserToCognitoUserUtil);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {

    }
}
