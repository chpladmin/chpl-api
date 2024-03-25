package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("acbActivityMetadataBuilder")
public class CertificationBodyActivityMetadataBuilder extends ActivityMetadataBuilder {
    private ObjectMapper jsonMapper;

    @Autowired
    public CertificationBodyActivityMetadataBuilder(CognitoUserService cognitoUserService, UserDAO userDAO) {
        super(cognitoUserService, userDAO);
        jsonMapper = new ObjectMapper();
    }

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof CertificationBodyActivityMetadata)) {
            return;
        }
        CertificationBodyActivityMetadata acbMetadata = (CertificationBodyActivityMetadata) metadata;

        // parse acb specific metadata
        CertificationBody origAcb = null;
        if (dto.getOriginalData() != null) {
            try {
                origAcb = jsonMapper.readValue(dto.getOriginalData(), CertificationBody.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data. " + "JSON was: "
                        + dto.getOriginalData(), ex);
            }
        }

        CertificationBody newAcb = null;
        if (dto.getNewData() != null) {
            try {
                newAcb = jsonMapper.readValue(dto.getNewData(), CertificationBody.class);
            } catch (final Exception ex) {
                LOGGER.error(
                        "Could not parse activity ID " + dto.getId() + " new data. " + "JSON was: " + dto.getNewData(),
                        ex);
            }
        }

        if (newAcb != null) {
            // if there is a new acb that could mean the acb
            // was updated and we want to fill in the metadata with the
            // latest acb info
            parseAcbMetadata(acbMetadata, newAcb);
        } else if (origAcb != null) {
            // if there is an original acb but no new acb
            // then the acb was deleted - pull its info from the orig object
            parseAcbMetadata(acbMetadata, origAcb);
        }

        acbMetadata.getCategories().add(ActivityCategory.CERTIFICATION_BODY);
    }

    private void parseAcbMetadata(final CertificationBodyActivityMetadata acbMetadata, final CertificationBody acb) {
        acbMetadata.setAcbId(acb.getId());
        acbMetadata.setAcbName(acb.getName());
    }
}
