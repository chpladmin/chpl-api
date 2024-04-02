package gov.healthit.chpl.activity;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("functionalityTestedActivityMetadataBuilder")
public class FunctionalityTestedActivityMetadataBuilder extends ActivityMetadataBuilder {
    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ObjectMapper jsonMapper = new ObjectMapper();
        FunctionalityTested funcTested = null;

        if (metadata.getCategories().contains(ActivityCategory.CREATE)) {
            try {
                funcTested = jsonMapper.readValue(dto.getNewData(), FunctionalityTested.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as FunctionalityTested. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (metadata.getCategories().contains(ActivityCategory.DELETE)
                || metadata.getCategories().contains(ActivityCategory.UPDATE)) {
            try {
                funcTested = jsonMapper.readValue(dto.getOriginalData(), FunctionalityTested.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as FunctionalityTested. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (funcTested != null) {
            metadata.getObject().setName(funcTested.getRegulatoryTextCitation());
        }
    }
}
