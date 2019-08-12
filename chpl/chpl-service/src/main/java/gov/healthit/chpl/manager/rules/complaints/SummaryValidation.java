package gov.healthit.chpl.manager.rules.complaints;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class SummaryValidation extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(SummaryValidation.class);

    public SummaryValidation() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        // Summary is required
        if (StringUtils.isEmpty(context.getComplaintDTO().getSummary())) {
            getMessages().add(getErrorMessage("complaints.summary.required"));
            return false;
        }
        return true;
    }
}
