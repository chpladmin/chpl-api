package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;

@Component("acbActivityMetadataBuilder")
public class CertificationBodyActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(CertificationBodyActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;

    public CertificationBodyActivityMetadataBuilder() {
        super();
        jsonMapper = new ObjectMapper();
    }

    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof CertificationBodyActivityMetadata)) {
            return;
        }
        CertificationBodyActivityMetadata acbMetadata = (CertificationBodyActivityMetadata) metadata;

        //parse acb specific metadata
        CertificationBodyDTO origAcb = null;
        if (dto.getOriginalData() != null) {
            try {
                origAcb =
                    jsonMapper.readValue(dto.getOriginalData(), CertificationBodyDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data. "
                        + "JSON was: " + dto.getOriginalData(), ex);
            }
        }

        CertificationBodyDTO newAcb = null;
        if (dto.getNewData() != null) {
            try {
                newAcb =
                    jsonMapper.readValue(dto.getNewData(), CertificationBodyDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " new data. "
                        + "JSON was: " + dto.getNewData(), ex);
            }
        }

        if (newAcb != null) {
            //if there is a new acb that could mean the acb
            //was updated and we want to fill in the metadata with the
            //latest acb info
            parseAcbMetadata(acbMetadata, newAcb);
        } else if (origAcb != null) {
            //if there is an original acb but no new acb
            //then the acb was deleted - pull its info from the orig object
            parseAcbMetadata(acbMetadata, origAcb);
        }

        acbMetadata.getCategories().add(ActivityCategory.CERTIFICATION_BODY);
    }

    private void parseAcbMetadata(
            final CertificationBodyActivityMetadata acbMetadata, final CertificationBodyDTO acb) {
        acbMetadata.setAcbId(acb.getId());
        acbMetadata.setAcbName(acb.getName());
    }
}
