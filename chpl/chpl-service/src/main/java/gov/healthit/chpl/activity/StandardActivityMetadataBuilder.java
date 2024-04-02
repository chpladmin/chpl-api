package gov.healthit.chpl.activity;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.standard.Standard;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("standardActivityMetadataBuilder")
public class StandardActivityMetadataBuilder extends ActivityMetadataBuilder {
    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ObjectMapper jsonMapper = new ObjectMapper();
        Standard standard = null;

        if (metadata.getCategories().contains(ActivityCategory.CREATE)) {
            try {
                standard = jsonMapper.readValue(dto.getNewData(), Standard.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Standard. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (metadata.getCategories().contains(ActivityCategory.DELETE)
                || metadata.getCategories().contains(ActivityCategory.UPDATE)) {
            try {
                standard = jsonMapper.readValue(dto.getOriginalData(), Standard.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Standard. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (standard != null) {
            metadata.getObject().setName(standard.getRegulatoryTextCitation());
        }
    }
}
