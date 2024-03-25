package gov.healthit.chpl.activity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ChangeRequestActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("changeRequestActivityMetadataBuilder")
public class ChangeRequestActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Autowired
    public ChangeRequestActivityMetadataBuilder(CognitoUserService cognitoUserService, UserDAO userDAO) {
        super(cognitoUserService, userDAO);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ChangeRequestActivityMetadata crActivityMetadata = (ChangeRequestActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();
        String json = "";
        try {
            json = getChangeRequestJson(dto);

            if (!StringUtils.isEmpty(json)) {
                ChangeRequest cr = jsonMapper.readValue(json, ChangeRequest.class);
                crActivityMetadata.setDeveloper(cr.getDeveloper());
                crActivityMetadata.getCertificationBodies().addAll(cr.getCertificationBodies());
            } else {
                logError(dto.getId(), json);
            }
        } catch (Exception e) {
            logError(dto.getId(), json);
        }
    }

    private void logError(final Long activityId, final String json) {
        LOGGER.error("Could not parse activity ID " + activityId + " original data " + "as Developer. "
                + "JSON was: " + json);
    }

    private String getChangeRequestJson(final ActivityDTO activityDTO) {
        String json = "";
        if (!StringUtils.isEmpty(activityDTO.getNewData())) {
            json = activityDTO.getNewData();
        } else if (!StringUtils.isEmpty(activityDTO.getOriginalData())) {
            json = activityDTO.getOriginalData();
        }
        return json;
    }
}
