package gov.healthit.chpl.manager.rules.complaints;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintTypeValid extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintTypeValid.class);
    private ComplaintValidationContext context;

    public ComplaintTypeValid() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        this.context = context;
        try {
            if (doesComplaintTypeExist(context.getComplaintDTO().getComplaintType().getId())) {
                return true;
            } else {
                getMessages().add(getErrorMessage("complaints.complaintType.notExists"));
                return false;
            }
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
