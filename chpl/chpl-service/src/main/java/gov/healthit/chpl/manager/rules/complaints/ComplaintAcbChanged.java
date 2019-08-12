package gov.healthit.chpl.manager.rules.complaints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.ComplaintDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintAcbChanged extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintAcbChanged.class);

    public ComplaintAcbChanged() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        try {
            // Get the original complaint
            ComplaintDTO originalDTO = context.getComplaintDAO().getComplaint(context.getComplaintDTO().getId());
            if (context.getComplaintDTO().getCertificationBody().getId()
                    .equals(originalDTO.getCertificationBody().getId())) {
                return true;
            } else {
                getMessages().add(getErrorMessage("complaints.update.acbChange"));
                return false;
            }
        } catch (Exception e) {
            String error = getErrorMessage("complaints.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

}
