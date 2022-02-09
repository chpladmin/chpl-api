package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestNotUpdatableDueToStatusValidation extends ValidationRule<ChangeRequestValidationContext> {


    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        try {
            if (getNonUpdatableStatuses(context).contains(context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId())) {
                getMessages().add(getErrorMessage(context, context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType()));
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            // This will be caught by other validators
            return true;
        }
    }

    private List<Long> getNonUpdatableStatuses(ChangeRequestValidationContext context) {
        return new ArrayList<Long>(Arrays.asList(
                context.getChangeRequestStatusIds().getAcceptedStatus(),
                context.getChangeRequestStatusIds().getCancelledStatus(),
                context.getChangeRequestStatusIds().getRejectedStatus()));
    }

    private String getErrorMessage(ChangeRequestValidationContext context, ChangeRequestStatusType changeRequestStatusType) {
        if (changeRequestStatusType.getId().equals(context.getChangeRequestStatusIds().getCancelledStatus())) {
            return getErrorMessage("changeRequest.status.cancelled");
        } else if (changeRequestStatusType.getId().equals(context.getChangeRequestStatusIds().getAcceptedStatus())) {
            return getErrorMessage("changeRequest.status.approved");
        } else if (changeRequestStatusType.getId().equals(context.getChangeRequestStatusIds().getRejectedStatus())) {
            return getErrorMessage("changeRequest.status.rejected");
        } else {
            return "";
        }
    }
}
