package gov.healthit.chpl.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ComplaintActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.ComplaintDTO;

@Component("complaintActivityMetadataBuilder")
public class ComplaintActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintActivityMetadataBuilder.class);

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ComplaintActivityMetadata complaintActivityMetadata = (ComplaintActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();
        String json = "";
        try {
            json = getComplaintJson(dto);

            if (!StringUtils.isEmpty(json)) {
                ComplaintDTO complaint = jsonMapper.readValue(json, ComplaintDTO.class);
                complaintActivityMetadata.setCertificationBody(new CertificationBody(complaint.getCertificationBody()));
            } else {
                logError(dto.getId(), json);
            }
        } catch (Exception e) {
            logError(dto.getId(), json);
        }
    }

    private void logError(final Long activityId, final String json) {
        LOGGER.error("Could not parse activity ID " + activityId + " original data " + "as ComplaintDTO. "
                + "JSON was: " + json);
    }

    private String getComplaintJson(final ActivityDTO activityDTO) {
        String json = "";
        if (!StringUtils.isEmpty(activityDTO.getNewData())) {
            json = activityDTO.getNewData();
        } else if (!StringUtils.isEmpty(activityDTO.getOriginalData())) {
            json = activityDTO.getOriginalData();
        }
        return json;
    }
}
