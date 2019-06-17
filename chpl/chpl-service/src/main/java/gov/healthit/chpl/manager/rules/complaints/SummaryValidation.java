package gov.healthit.chpl.manager.rules.complaints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class SummaryValidation extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(SummaryValidation.class);
    private ComplaintValidationContext context;

    public SummaryValidation() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        this.context = context;
        // Summary is required
        if (context.getComplaintDTO().getSummary() == null) {
            getMessages().add(getErrorMessage("complaints.summary.required"));
            return false;
        }
        return true;
    }
}
