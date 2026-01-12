package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Component("svapActivityMetadataBuilder")
public class SvapActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Autowired
    public SvapActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil,
            JsonMapper jsonMapper) {
        super(chplUserToCognitoUserUtil, jsonMapper);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        Svap svap = null;

        if (metadata.getCategories().contains(ActivityCategory.CREATE)) {
            try {
                svap = getJsonMapper().readValue(dto.getNewData(), Svap.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Svap. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (metadata.getCategories().contains(ActivityCategory.DELETE)
                || metadata.getCategories().contains(ActivityCategory.UPDATE)) {
            try {
                svap = getJsonMapper().readValue(dto.getOriginalData(), Svap.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Svap. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (svap != null) {
            metadata.getObject().setName(svap.getRegulatoryTextCitation());
        }
    }
}
