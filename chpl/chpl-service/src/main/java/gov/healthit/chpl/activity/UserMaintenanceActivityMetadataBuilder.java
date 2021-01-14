package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.UserMaintenanceActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.auth.UserDTO;

@Component("userMaintenanceActivityMetadataBuilder")
public class UserMaintenanceActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(UserMaintenanceActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;

    public UserMaintenanceActivityMetadataBuilder() {
        super();
        jsonMapper = new ObjectMapper();
    }

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof UserMaintenanceActivityMetadata)) {
            return;
        }
        UserMaintenanceActivityMetadata userMetadata = (UserMaintenanceActivityMetadata) metadata;

        UserDTO origUser = getUserDtoFromJson(dto.getOriginalData(), dto.getId());
        UserDTO newUser = getUserDtoFromJson(dto.getNewData(), dto.getId());

        if (newUser != null) {
            parseMetaData(newUser, userMetadata);
        } else if (origUser != null) {
            parseMetaData(origUser, userMetadata);
        }

        userMetadata.getCategories().add(ActivityCategory.USER_MAINTENANCE);
    }

    private void parseMetaData(UserDTO user, UserMaintenanceActivityMetadata userMetadata) {
        userMetadata.setEmail(user.getEmail());
        userMetadata.setSubjectName(user.getUsername());
    }

    private UserDTO getUserDtoFromJson(String json, Long dtoId) {
        UserDTO userDTO = null;
        try {
            if (json != null && json != "") {
                userDTO = jsonMapper.readValue(json, UserDTO.class);
            }
        } catch (final Exception ex) {
            LOGGER.error("Could not parse activity ID " + dtoId + " original data. " + "JSON was: " + json, ex);
        }
        return userDTO;
    }

}
