package gov.healthit.chpl.activity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ComplaintActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Component("complaintActivityMetadataBuilder")
@Log4j2
public class ComplaintActivityMetadataBuilder extends ActivityMetadataBuilder {

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ComplaintActivityMetadata complaintActivityMetadata = (ComplaintActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();
        String json = "";
        try {
            json = getComplaintJson(dto);

            if (!StringUtils.isEmpty(json)) {
                Complaint complaint = jsonMapper.readValue(json, Complaint.class);
                complaintActivityMetadata.setCertificationBody(complaint.getCertificationBody());
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
