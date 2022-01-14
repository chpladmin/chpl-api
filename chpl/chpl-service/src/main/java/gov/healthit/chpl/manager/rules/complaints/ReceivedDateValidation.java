package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ReceivedDateValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean getErrorMessages(ComplaintValidationContext context) {
        // Received Date type is required
        if (context.getComplaint().getReceivedDate() == null) {
            getMessages().add(getErrorMessage("complaints.receivedDate.required"));
            return false;
        }
        return true;
    }

}
