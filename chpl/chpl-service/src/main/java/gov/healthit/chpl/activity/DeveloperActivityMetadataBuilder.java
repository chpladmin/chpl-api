package gov.healthit.chpl.activity;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.DeveloperActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;

@Component("developerActivityMetadataBuilder")
public class DeveloperActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;

    public DeveloperActivityMetadataBuilder() {
        super();
        jsonMapper = new ObjectMapper();
    }

    protected void addConceptSpecificMetadata(final ActivityDTO activity, final ActivityMetadata metadata) {
        if (!(metadata instanceof DeveloperActivityMetadata)) {
            return;
        }
        DeveloperActivityMetadata developerMetadata = (DeveloperActivityMetadata) metadata;

        //parse developer specific metadata
        //for merges, the original data is a list of developers.
        //for other developer activities it's just a single developer.
        Developer origDeveloper = null;
        List<Developer> origDevelopers = null;
        if (activity.getOriginalData() != null) {
            try {
                origDeveloper =
                    jsonMapper.readValue(activity.getOriginalData(), Developer.class);
            } catch (final Exception ignore) {
            }

            //if we couldn't parse it as a Developer
            //try to parse it as a List.
            if (origDeveloper == null) {
                try {
                    origDevelopers = jsonMapper.readValue(activity.getOriginalData(),
                            jsonMapper.getTypeFactory().constructCollectionType(List.class, Developer.class));
                } catch (final Exception ignore) {
                }
            }

            //if the orig data is not a developer or a list, log an error
            if (origDeveloper == null && origDevelopers == null) {
                LOGGER.error("Could not parse activity ID " + activity.getId() + " original data as "
                    + "a Developer or List<Developer>. JSON was: " + activity.getOriginalData());
            }
        }

        Developer newDeveloper = null;
        List<Developer> newDevelopers = null;
        if (activity.getNewData() != null) {
            try {
                newDeveloper =
                    jsonMapper.readValue(activity.getNewData(), Developer.class);
            } catch (final Exception ignore) {
            }

            //if we couldn't parse it as a Developer
            //try to parse it as a List.
            if (newDeveloper == null) {
                try {
                    newDevelopers = jsonMapper.readValue(activity.getNewData(),
                            jsonMapper.getTypeFactory().constructCollectionType(List.class, Developer.class));
                } catch (final Exception ignore) {
                }
            }

            //if the new data is not a developer or a list, log an error
            if (newDeveloper == null && newDevelopers == null) {
                LOGGER.error("Could not parse activity ID " + activity.getId() + " new data as "
                    + "a Developer or List<Developer>. JSON was: " + activity.getNewData());
            }
        }

        if (newDeveloper != null && origDeveloper != null
                && newDevelopers == null && origDevelopers == null) {
            //if there is a single new developer and single original developer
            //that means the activity was editing the developer
            parseDeveloperMetadata(developerMetadata, newDeveloper);
        } else if (origDeveloper != null && newDeveloper == null
                && newDevelopers == null && origDevelopers == null) {
            //if there is an original developer but no new developer
            //then the developer was deleted - pull its info from the orig object
            parseDeveloperMetadata(developerMetadata, origDeveloper);
        } else if (newDeveloper != null && origDeveloper == null
                && newDevelopers == null && origDevelopers == null) {
            //if there is a new developer but no original developer
            //then the developer was just created
            parseDeveloperMetadata(developerMetadata, newDeveloper);
        } else if (newDevelopers != null && origDeveloper != null
                && newDeveloper == null && origDevelopers == null) {
            //multiple new developers and a single original developer
            //means the activity was a split
            parseDeveloperMetadata(developerMetadata, activity, newDevelopers);
        } else if (origDevelopers != null && newDeveloper != null
                && origDeveloper == null && newDevelopers == null) {
            //multiple original developers and a single new developer
            //means the activity was a merge
            parseDeveloperMetadata(developerMetadata, newDeveloper);
        }

        developerMetadata.getCategories().add(ActivityCategory.DEVELOPER);
    }

    private void parseDeveloperMetadata(DeveloperActivityMetadata developerMetadata, Developer developer) {
        developerMetadata.setDeveloperName(developer.getName());
        developerMetadata.setDeveloperCode(developer.getDeveloperCode());
    }

    /**
     * Find the developer in the list that matches the id of the developer
     * the activity was recorded for. Parse activity metadata from that developer.
     * @param developerMetadata
     * @param activity
     * @param developers
     */
    private void parseDeveloperMetadata(
            DeveloperActivityMetadata developerMetadata, ActivityDTO activity,
            List<Developer> developers) {
        Long idToFind = activity.getActivityObjectId();
        for (Developer currDev : developers) {
            if (currDev != null && currDev.getDeveloperId().longValue() == idToFind.longValue()) {
                parseDeveloperMetadata(developerMetadata, currDev);
                break;
            }
        }
    }
}
