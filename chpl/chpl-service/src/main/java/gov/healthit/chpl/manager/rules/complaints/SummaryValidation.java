package gov.healthit.chpl.manager.rules.complaints;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class SummaryValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean getErrorMessages(ComplaintValidationContext context) {
        // Summary is required
        if (StringUtils.isEmpty(context.getComplaint().getSummary())) {
            getMessages().add(getErrorMessage("complaints.summary.required"));
            return false;
        }
        return true;
    }
}
