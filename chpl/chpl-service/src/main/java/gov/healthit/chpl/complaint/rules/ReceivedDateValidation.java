package gov.healthit.chpl.complaint.rules;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ReceivedDateValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        // Received Date type is required
        if (context.getComplaint().getReceivedDate() == null) {
            getMessages().add(getErrorMessage("complaints.receivedDate.required"));
            return false;
        }
        return true;
    }

}
