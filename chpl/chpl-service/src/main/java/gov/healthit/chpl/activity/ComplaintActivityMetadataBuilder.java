package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ComplaintActivityMetadata;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.ActivityDTO;

@Component("complaintActivityMetadataBuilder")
public class ComplaintActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintActivityMetadataBuilder.class);

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ComplaintActivityMetadata complaintActivityMetadata = (ComplaintActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            Complaint complaint = jsonMapper.readValue(dto.getNewData(), Complaint.class);
            complaintActivityMetadata.setCertificationBody(complaint.getCertificationBody());
        } catch (Exception e) {
            LOGGER.error("Could not parse activity ID " + dto.getId() + " original data " + "as ComplaintDTO. "
                    + "JSON was: " + dto.getNewData());
        }
    }

}
