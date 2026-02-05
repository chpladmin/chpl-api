package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.DeveloperActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Component("developerActivityMetadataBuilder")
public class DeveloperActivityMetadataBuilder extends ActivityMetadataBuilder {
    private DeveloperDAO developerDao;

    @Autowired
    public DeveloperActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil,
            JsonMapper jsonMapper,
            DeveloperDAO developerDao) {
        super(chplUserToCognitoUserUtil, jsonMapper);
        this.developerDao = developerDao;
    }

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO activity, final ActivityMetadata metadata) {
        if (!(metadata instanceof DeveloperActivityMetadata)) {
            return;
        }

        DeveloperActivityMetadata developerMetadata = (DeveloperActivityMetadata) metadata;
        developerMetadata.getCategories().add(ActivityCategory.DEVELOPER);

        if (metadata.getObject() != null && metadata.getObject().getId() != null) {
            Developer dev = null;
            try {
                dev = developerDao.getById(metadata.getObject().getId(), true);
                metadata.getObject().setName(dev.getName());
            } catch (Exception ex) {
                LOGGER.error("Could not find developer " + metadata.getObject().getId() + " for activity metadata.", ex);
            }
        }
    }
}
