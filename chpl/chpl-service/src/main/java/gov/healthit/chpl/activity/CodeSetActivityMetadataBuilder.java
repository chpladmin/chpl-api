package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Component("codeSetActivityMetadataBuilder")
public class CodeSetActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Autowired
    public CodeSetActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil,
            JsonMapper jsonMapper) {
        super(chplUserToCognitoUserUtil, jsonMapper);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        CodeSet codeSet = null;

        if (metadata.getCategories().contains(ActivityCategory.CREATE)) {
            try {
                codeSet = getJsonMapper().readValue(dto.getNewData(), CodeSet.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Code Set. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (metadata.getCategories().contains(ActivityCategory.DELETE)
                || metadata.getCategories().contains(ActivityCategory.UPDATE)) {
            try {
                codeSet = getJsonMapper().readValue(dto.getOriginalData(), CodeSet.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Code Set. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (codeSet != null) {
            metadata.getObject().setName(codeSet.getName());
        }
    }
}
