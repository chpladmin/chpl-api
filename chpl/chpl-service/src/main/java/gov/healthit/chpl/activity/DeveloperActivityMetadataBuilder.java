package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.DeveloperActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.DeveloperDTO;

@Component("developerActivityMetadataBuilder")
public class DeveloperActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;

    public DeveloperActivityMetadataBuilder() {
        super();
        jsonMapper = new ObjectMapper();
    }

    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof DeveloperActivityMetadata)) {
            return;
        }
        DeveloperActivityMetadata developerMetadata = (DeveloperActivityMetadata) metadata;

        //parse developer specific metadata
        DeveloperDTO origDeveloper = null;
        if (dto.getOriginalData() != null) {
            try {
                origDeveloper =
                    jsonMapper.readValue(dto.getOriginalData(), DeveloperDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data. "
                        + "JSON was: " + dto.getOriginalData(), ex);
            }
        }

        DeveloperDTO newDeveloper = null;
        if (dto.getNewData() != null) {
            try {
                newDeveloper =
                    jsonMapper.readValue(dto.getNewData(), DeveloperDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " new data. "
                        + "JSON was: " + dto.getNewData(), ex);
            }
        }

        if (newDeveloper != null) {
            //if there is a new developer that could mean the developer
            //was updated and we want to fill in the metadata with the
            //latest developer info
            parseDeveloperMetadata(developerMetadata, newDeveloper);
        } else if (origDeveloper != null) {
            //if there is an original deveoper but no new developer
            //then the developer was deleted - pull its info from the orig object
            parseDeveloperMetadata(developerMetadata, origDeveloper);
        }

        developerMetadata.getCategories().add(ActivityCategory.DEVELOPER);
    }

    private void parseDeveloperMetadata(
            final DeveloperActivityMetadata developerMetadata, final DeveloperDTO developer) {
        developerMetadata.setDeveloperName(developer.getName());
        developerMetadata.setDeveloperCode(developer.getDeveloperCode());
    }
}
