package gov.healthit.chpl.activity;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("apiKeyActivityMetadataBuilder")
public class ApiKeyActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ObjectMapper jsonMapper = new ObjectMapper();
        ApiKey apiKey = null;

        if (metadata.getCategories().contains(ActivityCategory.CREATE)) {
            try {
                apiKey = jsonMapper.readValue(dto.getNewData(), ApiKey.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as ApiKey. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (metadata.getCategories().contains(ActivityCategory.DELETE)
                || metadata.getCategories().contains(ActivityCategory.UPDATE)) {
            try {
                apiKey = jsonMapper.readValue(dto.getOriginalData(), ApiKey.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as ApiKey. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (apiKey != null) {
            metadata.getObject().setName(apiKey.getKey());
        }
    }
}
