package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestRejectedValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Value("${changerequest.status.rejected}")
    private Long rejectedStatus;

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // We need to check the current status from the DB - not when the user
        // has passed in
        try {
            ChangeRequest cr = context.getChangeRequestDAO().get(context.getChangeRequest().getId());
            if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(rejectedStatus)) {
                getMessages().add(getErrorMessage("changeRequest.status.approved"));
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            // This will be caught by other validators
            return true;
        }
    }

}
