package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Component("conformanceMethodActivityMetadataBuilder")
public class ConformanceMethodActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Autowired
    public ConformanceMethodActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil,
            JsonMapper jsonMapper) {
        super(chplUserToCognitoUserUtil, jsonMapper);
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ConformanceMethod conformanceMethod = null;

        if (metadata.getCategories().contains(ActivityCategory.CREATE)) {
            try {
                conformanceMethod = getJsonMapper().readValue(dto.getNewData(), ConformanceMethod.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as ConformanceMethod. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (metadata.getCategories().contains(ActivityCategory.DELETE)
                || metadata.getCategories().contains(ActivityCategory.UPDATE)) {
            try {
                conformanceMethod = getJsonMapper().readValue(dto.getOriginalData(), ConformanceMethod.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as ConformanceMethod. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (conformanceMethod != null) {
            metadata.getObject().setName(conformanceMethod.getName());
        }
    }
}
