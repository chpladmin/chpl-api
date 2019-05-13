package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.TestingLabActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.TestingLabDTO;

@Component("atlActivityMetadataBuilder")
public class TestingLabActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(TestingLabActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;

    public TestingLabActivityMetadataBuilder() {
        super();
        jsonMapper = new ObjectMapper();
    }

    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof TestingLabActivityMetadata)) {
            return;
        }
        TestingLabActivityMetadata atlMetadata = (TestingLabActivityMetadata) metadata;

        //parse atl specific metadata
        TestingLabDTO origAtl = null;
        if (dto.getOriginalData() != null) {
            try {
                origAtl =
                    jsonMapper.readValue(dto.getOriginalData(), TestingLabDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data. "
                        + "JSON was: " + dto.getOriginalData(), ex);
            }
        }

        TestingLabDTO newAtl = null;
        if (dto.getNewData() != null) {
            try {
                newAtl =
                    jsonMapper.readValue(dto.getNewData(), TestingLabDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " new data. "
                        + "JSON was: " + dto.getNewData(), ex);
            }
        }

        if (newAtl != null) {
            //if there is a new acb that could mean the acb
            //was updated and we want to fill in the metadata with the
            //latest acb info
            parseAtlMetadata(atlMetadata, newAtl);
        } else if (origAtl != null) {
            //if there is an original acb but no new acb
            //then the acb was deleted - pull its info from the orig object
            parseAtlMetadata(atlMetadata, origAtl);
        }

        atlMetadata.getCategories().add(ActivityCategory.TESTING_LAB);
    }

    private void parseAtlMetadata(
            final TestingLabActivityMetadata atlMetadata, final TestingLabDTO atl) {
        atlMetadata.setAtlId(atl.getId());
        atlMetadata.setAtlName(atl.getName());
    }
}
