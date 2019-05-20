package gov.healthit.chpl.activity;

import java.util.List;

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
        //for merges, the original data is a list of developers.
        //for other developer activities it's just a single developer.
        DeveloperDTO origDeveloper = null;
        List<DeveloperDTO> origDevelopers = null;
        if (dto.getOriginalData() != null) {
            try {
                origDeveloper =
                    jsonMapper.readValue(dto.getOriginalData(), DeveloperDTO.class);
            } catch (final Exception ignore) {}

            //if we couldn't parse it as a DeveloperDTO
            //try to parse it as a List.
            if (origDeveloper == null) {
                try {
                    origDevelopers =
                            jsonMapper.readValue(dto.getOriginalData(), List.class);
                } catch (final Exception ignore) {
                }
            }

            //if the orig data is not a developer or a list, log an error
            if (origDeveloper == null && origDevelopers == null) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data as "
                    + "a DeveloperDTO or List<DeveloperDTO>. JSON was: " + dto.getOriginalData());
            }
        }

        DeveloperDTO newDeveloper = null;
        List<DeveloperDTO> newDevelopers = null;
        if (dto.getNewData() != null) {
            try {
                newDeveloper =
                    jsonMapper.readValue(dto.getNewData(), DeveloperDTO.class);
            } catch (final Exception ignore) { }

            //if we couldn't parse it as a DeveloperDTO
            //try to parse it as a List.
            if (newDeveloper == null) {
                try {
                    newDevelopers =
                            jsonMapper.readValue(dto.getOriginalData(), List.class);
                } catch (final Exception ignore) {
                }
            }

            //if the new data is not a developer or a list, log an error
            if (newDeveloper == null && newDevelopers == null) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " new data as "
                    + "a DeveloperDTO or List<DeveloperDTO>. JSON was: " + dto.getNewData());
            }
        }

        if (newDeveloper != null) {
            //if there is a new developer that could mean the developer
            //was updated and we want to fill in the metadata with the
            //latest developer info
            parseDeveloperMetadata(developerMetadata, newDeveloper);
        } else if (origDeveloper != null) {
            //if there is an original developer but no new developer
            //then the developer was deleted - pull its info from the orig object
            parseDeveloperMetadata(developerMetadata, origDeveloper);
        } else if (newDevelopers != null && newDevelopers.size() > 0) {
            //there could be multiple new developers on a developer split
            parseDeveloperMetadata(developerMetadata, newDevelopers.get(0));
        } else if (origDevelopers != null && origDevelopers.size() > 0) {
            //there could be multiple original developers on a developer merge
            parseDeveloperMetadata(developerMetadata, origDevelopers.get(0));
        }

        developerMetadata.getCategories().add(ActivityCategory.DEVELOPER);
    }

    private void parseDeveloperMetadata(
            final DeveloperActivityMetadata developerMetadata, final DeveloperDTO developer) {
        developerMetadata.setDeveloperName(developer.getName());
        developerMetadata.setDeveloperCode(developer.getDeveloperCode());
    }
}
