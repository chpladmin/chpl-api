package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestNotUpdatableDueToStatusValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledStatus;

    @Value("${changerequest.status.accepted}")
    private Long acceptedStatus;

    @Value("${changerequest.status.rejected}")
    private Long rejectedStatus;

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // We need to check the current status from the DB - not what the user
        // has passed in
        try {
            ChangeRequest cr = context.getChangeRequestDAO().get(context.getChangeRequest().getId());

            if (getNonUpdatableStatuses().contains(cr.getCurrentStatus().getChangeRequestStatusType().getId())) {
                getMessages().add(getErrorMessage(cr.getCurrentStatus().getChangeRequestStatusType()));
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            // This will be caught by other validators
            return true;
        }
    }

    private List<Long> getNonUpdatableStatuses() {
        List<Long> statuses = new ArrayList<Long>();
        statuses.add(acceptedStatus);
        statuses.add(cancelledStatus);
        statuses.add(rejectedStatus);
        return statuses;
    }

    private String getErrorMessage(ChangeRequestStatusType changeRequestStatusType) {
        if (changeRequestStatusType.getId().equals(cancelledStatus)) {
            return getErrorMessage("changeRequest.status.cancelled");
        } else if (changeRequestStatusType.getId().equals(acceptedStatus)) {
            return getErrorMessage("changeRequest.status.accepted");
        } else if (changeRequestStatusType.getId().equals(rejectedStatus)) {
            return getErrorMessage("changeRequest.status.rejected");
        } else {
            return "";
        }
    }
}
