package gov.healthit.chpl.manager.rules.complaints;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintTypeValidation extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintTypeValidation.class);
    private ComplaintValidationContext context;

    public ComplaintTypeValidation() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        this.context = context;
        try {
            // Complaint type is required
            if (context.getComplaintDTO().getComplaintType() == null
                    || context.getComplaintDTO().getComplaintType().getId() == null) {
                getMessages().add(getErrorMessage("complaints.complaintType.required"));
                return false;
            }

            // Complaint type value must exist
            if (!doesComplaintTypeExist(context.getComplaintDTO().getComplaintType().getId())) {
                getMessages().add(getErrorMessage("complaints.complaintType.notExists"));
                return false;
            }
            return true;
        } catch (Exception e) {
            String error = getErrorMessage("complaints.complaintType.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

    private Boolean doesComplaintTypeExist(long complaintTypeId) {
        List<ComplaintTypeDTO> complaintTypes = context.getComplaintDAO().getComplaintTypes();
        for (ComplaintTypeDTO complaintType : complaintTypes) {
            if (complaintType.getId().equals(complaintTypeId)) {
                return true;
            }
        }
        return false;
    }

}
