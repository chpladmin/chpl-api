package gov.healthit.chpl.manager.rules.complaints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintStatusOpen extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintStatusOpen.class);
    private ComplaintValidationContext context;

    public ComplaintStatusOpen() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        try {
            this.context = context;

            // Is the complaint status Open?
            if (context.getComplaintDTO().getComplaintStatusType().getId()
                    .equals(getComplaintStatusType("Open").getId())) {
                return true;
            } else {
                getMessages().add(getErrorMessage("complaints.create.statusOpen"));
                return false;
            }

        } catch (Exception e) {
            String error = getErrorMessage("complaints.create.statusOpen.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

    private ComplaintStatusTypeDTO getComplaintStatusType(String name) {
        for (ComplaintStatusTypeDTO dto : context.getComplaintDAO().getComplaintStatusTypes()) {
            if (dto.getName().equals(name)) {
                return dto;
            }
        }
        return null;
    }
}
