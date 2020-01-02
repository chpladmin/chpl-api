package gov.healthit.chpl.manager.rules.complaints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            if (context.getComplaintDTO().getClosedDate() == null) {
                return true;
            } else {
                getMessages().add(getErrorMessage("complaints.create.statusOpen"));
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
