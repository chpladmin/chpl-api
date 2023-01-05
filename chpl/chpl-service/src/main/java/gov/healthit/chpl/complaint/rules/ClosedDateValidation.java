package gov.healthit.chpl.complaint.rules;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ClosedDateValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        if (context.getComplaint().getClosedDate() != null) {
            // Closed Date may not be in the future
            if (context.getComplaint().getClosedDate().isAfter(LocalDate.now())) {
                getMessages().add(getErrorMessage("complaints.closedDate.inTheFuture"));
                return false;
            }
            // Closed Date must be on or after the Received Date
            if (context.getComplaint().getReceivedDate() != null && context.getComplaint().getClosedDate().isBefore(context.getComplaint().getReceivedDate())) {
                getMessages().add(getErrorMessage("complaints.closedDate.mustBeAfterReceivedDate"));
                return false;
            }
            // Actions/Responses must be provided if a Closed Date is provided
            if (StringUtils.isEmpty(context.getComplaint().getActions())) {
                getMessages().add(getErrorMessage("complaints.closedDate.actionsRequired"));
                return false;
            }
        }
        return true;
    }
}
