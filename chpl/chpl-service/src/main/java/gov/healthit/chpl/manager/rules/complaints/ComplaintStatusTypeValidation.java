package gov.healthit.chpl.manager.rules.complaints;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintStatusTypeValidation extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintStatusTypeValidation.class);
    private ComplaintValidationContext context;

    public ComplaintStatusTypeValidation() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        this.context = context;
        try {
            if (context.getComplaintDTO().getComplaintStatusType() != null
                    && context.getComplaintDTO().getComplaintStatusType().getId() != null
                    && doesComplaintStatusTypeExist(context.getComplaintDTO().getComplaintStatusType().getId())) {
                return true;
            } else {
                getMessages().add(getErrorMessage("complaints.complaintStatusType.notExists"));
                return false;
            }
        } catch (Exception e) {
            String error = getErrorMessage("complaints.complaintStatusType.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

    private Boolean doesComplaintStatusTypeExist(long complaintStatusTypeId) {
        List<ComplaintStatusTypeDTO> complaintStatusTypes = context.getComplaintDAO().getComplaintStatusTypes();
        for (ComplaintStatusTypeDTO complaintStatusType : complaintStatusTypes) {
            if (complaintStatusType.getId().equals(complaintStatusTypeId)) {
                return true;
            }
        }
        return false;
    }

}
